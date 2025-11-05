rootProject.name = "auroraslanterns"

pluginManagement {
	repositories {
		gradlePluginPortal()
		maven {
			name = "Fabric"
			url = uri("https://maven.fabricmc.net/")
		}
		maven {
			name = "Gegy"
			url = uri("https://maven.gegy.dev/releases/")
			content {
				includeGroupAndSubgroups("dev.lambdaurora")
			}
		}
	}
}
