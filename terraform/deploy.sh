#!/usr/bin/env bash
set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Get script directory
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
TERRAFORM_DIR="$SCRIPT_DIR/terraform"
TFVARS_FILE="$TERRAFORM_DIR/environments/$1/$1.tfvars"

# Parse arguments
if [ $# -lt 2 ]; then
    echo -e "${RED}Usage: $0 <environment> <action> [--auto-approve]${NC}"
    echo -e "  environment: dev or prd"
    echo -e "  action: init, plan, apply, destroy, output"
    exit 1
fi

ENVIRONMENT=$1
ACTION=$2
AUTO_APPROVE=${3:-}

echo -e "${CYAN}======================================== ${NC}"
echo -e "${CYAN}Journal App Terraform Deployment${NC}"
echo -e "${YELLOW}Environment: $ENVIRONMENT${NC}"
echo -e "${YELLOW}Action: $ACTION${NC}"
echo -e "${CYAN}======================================== ${NC}"

cd "$TERRAFORM_DIR"

case $ACTION in
    init)
        echo -e "${GREEN}Initializing Terraform...${NC}"
        terraform init

        echo -e "${GREEN}Creating workspace: $ENVIRONMENT${NC}"
        if ! terraform workspace list | grep -q "^\s*$ENVIRONMENT\s*$"; then
            terraform workspace new "$ENVIRONMENT"
        fi
        terraform workspace select "$ENVIRONMENT"
        echo -e "${GREEN}Workspace '$ENVIRONMENT' is now active${NC}"
        ;;

    plan)
        echo -e "${GREEN}Selecting workspace: $ENVIRONMENT${NC}"
        terraform workspace select "$ENVIRONMENT"

        echo -e "${GREEN}Planning Terraform configuration...${NC}"
        terraform plan -var-file="$TFVARS_FILE" -out=tfplan
        ;;

    apply)
        echo -e "${GREEN}Selecting workspace: $ENVIRONMENT${NC}"
        terraform workspace select "$ENVIRONMENT"

        if [ ! -f "tfplan" ]; then
            echo -e "${YELLOW}tfplan not found. Running plan first...${NC}"
            terraform plan -var-file="$TFVARS_FILE" -out=tfplan
        fi

        if [ "$AUTO_APPROVE" = "--auto-approve" ]; then
            echo -e "${GREEN}Applying Terraform configuration (auto-approved)...${NC}"
            terraform apply -auto-approve tfplan
        else
            echo -e "${GREEN}Applying Terraform configuration...${NC}"
            terraform apply tfplan
        fi

        rm -f tfplan

        echo -e "${GREEN}Deployment complete!${NC}"
        echo -e "${CYAN}Run '$0 $ENVIRONMENT output' to view outputs${NC}"
        ;;

    destroy)
        echo -e "${GREEN}Selecting workspace: $ENVIRONMENT${NC}"
        terraform workspace select "$ENVIRONMENT"

        echo -e "${RED}WARNING: This will destroy all resources in $ENVIRONMENT!${NC}"

        if [ "$AUTO_APPROVE" = "--auto-approve" ]; then
            echo -e "${RED}Destroying resources (auto-approved)...${NC}"
            terraform destroy -auto-approve -var-file="$TFVARS_FILE"
        else
            echo -e "${RED}Destroying resources...${NC}"
            terraform destroy -var-file="$TFVARS_FILE"
        fi
        ;;

    output)
        echo -e "${GREEN}Selecting workspace: $ENVIRONMENT${NC}"
        terraform workspace select "$ENVIRONMENT"

        echo -e "${GREEN}Terraform Outputs:${NC}"
        terraform output
        ;;

    *)
        echo -e "${RED}Unknown action: $ACTION${NC}"
        echo "Valid actions: init, plan, apply, destroy, output"
        exit 1
        ;;
esac

cd "$SCRIPT_DIR"

