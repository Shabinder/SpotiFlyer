terraform {
  backend "artifactory" {
    // -backend-config="username=xxx@xxx.com" \
    // -backend-config="password=xxxxxx" \
    url     = "https://spotiflyer.jfrog.io/artifactory"
    repo    = "terraform-state"
    subpath = "SpotiFlyer"
  }
}

provider "azurerm" {
  features {}
}

resource "azurerm_resource_group" "main" {
  location = "westeurope"
  name     = "SpotiFlyer"
}

resource "azurerm_application_insights" "main" {
  name                = azurerm_resource_group.main.name
  location            = azurerm_resource_group.main.location
  resource_group_name = azurerm_resource_group.main.name
  application_type    = "java"
}

resource "azurerm_app_service_plan" "main" {
  location            = azurerm_resource_group.main.location
  name                = azurerm_resource_group.main.name
  resource_group_name = azurerm_resource_group.main.name
  kind                = "Linux"
  reserved            = true
  sku {
    tier = "Free"
    size = "F1"
  }
}

resource "azurerm_app_service" "main" {
  resource_group_name = azurerm_app_service_plan.main.resource_group_name
  app_service_plan_id = azurerm_app_service_plan.main.id
  location            = azurerm_app_service_plan.main.location
  name                = azurerm_app_service_plan.main.name
  https_only          = true

  site_config {
    use_32_bit_worker_process = true
    app_command_line          = ""
    linux_fx_version          = "DOCKER|${var.docker_image_tag}"
    http2_enabled             = true
    cors {
      allowed_origins = ["*"]
    }
  }

  app_settings = {
    WEBSITES_ENABLE_APP_SERVICE_STORAGE = false
    DOCKER_REGISTRY_SERVER_URL          = var.docker_registry
    DOCKER_REGISTRY_SERVER_USERNAME     = var.docker_registry_username
    DOCKER_REGISTRY_SERVER_PASSWORD     = var.docker_registry_password
    AZURE_MONITOR_INSTRUMENTATION_KEY   = azurerm_application_insights.main.instrumentation_key
    APPINSIGHTS_INSTRUMENTATIONKEY      = azurerm_application_insights.main.instrumentation_key
    APPINSIGHTS_PROFILERFEATURE_VERSION = "1.0.0"
    WEBSITE_HTTPLOGGING_RETENTION_DAYS  = "35"
    CORSANYWHERE_ALLOWLIST  = var.cors_anywhere_allow_list
    CORSANYWHERE_RATELIMIT  = var.cors_anywhere_rate_limit
  }
}
