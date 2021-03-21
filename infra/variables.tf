variable "docker_registry" {
  type    = string
  default = "https://docker.pkg.github.com"
}

variable "docker_registry_username" {
  type    = string
}

variable "docker_registry_password" {
  type      = string
  sensitive = true
}

variable "docker_image_tag" {
  type = string
  default = "docker.pkg.github.com/shabinder/cors-anywhere/server:latest"
}

variable "cors_anywhere_allow_list" {
  type = string
  default = ""
}

variable "cors_anywhere_rate_limit" {
  type = string
  default = ""
}

