locals {
  oac_name = var.env == "prd" ? "${var.service_name}-photos-oac" : "${var.service_name}-photos-oac-${var.env}"
}

resource "aws_cloudfront_public_key" "main" {
  name = "${var.service_name}-cloudfront-public-key-${var.env}"
  encoded_key = var.cloudfront_encoded_public_key_value
  comment     = "Public key for CloudFront signed URLs"
}

resource "aws_cloudfront_key_group" "main" {
  comment     = "Key group for signing CloudFront URLs"
  items       = [aws_cloudfront_public_key.main.id]
  name        = "${var.service_name}-cloudfront-key-group-${var.env}"
}

resource "aws_cloudfront_origin_access_control" "main" {
  name                              = local.oac_name
  description                       = "Origin Access Control for S3"
  origin_access_control_origin_type = "s3"
  signing_behavior                  = "always"
  signing_protocol                  = "sigv4"
}

resource "aws_acm_certificate" "main" {
  domain_name       = var.domain_name
  validation_method = "DNS"

  subject_alternative_names = [
    "www.${var.domain_name}"
  ]

  lifecycle {
    create_before_destroy = true
  }

  tags = {
    service = var.service_name,
    env = var.env
  }
}

resource "aws_route53_record" "validation" {
  for_each = {
    for dvo in aws_acm_certificate.main.domain_validation_options : dvo.domain_name => {
      name   = dvo.resource_record_name
      record = dvo.resource_record_value
      type   = dvo.resource_record_type
    }
  }

  allow_overwrite = true
  name            = each.value.name
  records         = [each.value.record]
  ttl             = 60
  type            = each.value.type
  zone_id         = var.hosted_zone_id
}

resource "aws_acm_certificate_validation" "main" {
  certificate_arn           = aws_acm_certificate.main.arn
  timeouts {
    create = "5m"
  }
  depends_on = [aws_route53_record.validation]
}

resource "aws_cloudfront_distribution" "main" {
  enabled             = true
  default_root_object = ""
  is_ipv6_enabled     = true

  aliases = [
    var.domain_name,
    "www.${var.domain_name}"
  ]

  origin {
    domain_name              = var.s3_bucket_regional_domain_name
    origin_id                = "S3Origin"
    origin_access_control_id = aws_cloudfront_origin_access_control.main.id
  }

  default_cache_behavior {
    allowed_methods  = ["GET", "HEAD"]
    cached_methods   = ["GET", "HEAD"]
    target_origin_id = "S3Origin"

    viewer_protocol_policy = "redirect-to-https"

    forwarded_values {
      query_string = false

      cookies {
        forward = "none"
      }
    }

    trusted_key_groups = [aws_cloudfront_key_group.main.id]

    compress = true
  }

  viewer_certificate {
    acm_certificate_arn            = aws_acm_certificate_validation.main.certificate_arn
    ssl_support_method             = "sni-only"
    minimum_protocol_version       = "TLSv1.2_2021"
    cloudfront_default_certificate = false
  }

  restrictions {
    geo_restriction {
      restriction_type = "none"
    }
  }

  tags = {
    service = var.service_name,
    env = var.env
  }

  depends_on = [aws_acm_certificate_validation.main]
}

resource "aws_route53_record" "cloudfront_dns" {
  zone_id = var.hosted_zone_id
  name    = var.domain_name
  type    = "A"

  alias {
    name                   = aws_cloudfront_distribution.main.domain_name
    zone_id                = aws_cloudfront_distribution.main.hosted_zone_id
    evaluate_target_health = false
  }
}

resource "aws_route53_record" "cloudfront_www_dns" {
  zone_id = var.hosted_zone_id
  name    = "www.${var.domain_name}"
  type    = "A"

  alias {
    name                   = aws_cloudfront_distribution.main.domain_name
    zone_id                = aws_cloudfront_distribution.main.hosted_zone_id
    evaluate_target_health = false
  }
}