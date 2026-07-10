pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}

rootProject.name = "TitanIDE"

include(":app")

include(":core:common")
include(":core:database")
include(":core:datastore")
include(":core:network")
include(":core:security")
include(":core:ai")

include(":domain")
include(":data:repository")
include(":data:local")
include(":data:remote")

include(":feature:editor")
include(":feature:filemanager")
include(":feature:projectmanager")
include(":feature:terminal")
include(":feature:git")
include(":feature:buildsystem")
include(":feature:settings")
include(":feature:logcat")
include(":feature:debugger")
include(":feature:apktools")
include(":feature:xmldesigner")
include(":feature:ai")
include(":feature:plugins")
include(":feature:tools")