pluginManagement {
    repositories {
        google()
        // Google's mirror of Maven Central first; it keeps working when repo.maven.apache.org
        // redirects to GitHub release downloads that time out.
        maven("https://maven-central.storage-download.googleapis.com/maven2/")
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        maven("https://maven-central.storage-download.googleapis.com/maven2/")
        mavenCentral()
    }
}
rootProject.name = "AI Scanner"
include(":app")
