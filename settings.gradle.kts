pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
        google()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://naver.jfrog.io/artifactory/maven/")
        }
        maven {
            url = uri("https://devrepo.kakao.com/nexus/content/groups/public/")
        }
    }
}

rootProject.name = "TastyLog"
include(":app")
 