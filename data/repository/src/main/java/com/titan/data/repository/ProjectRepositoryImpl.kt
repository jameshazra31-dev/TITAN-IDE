package com.titan.data.repository

import com.titan.core.common.base.Result
import com.titan.core.database.dao.ProjectDao
import com.titan.core.database.entity.ProjectEntity
import com.titan.domain.model.Project
import com.titan.domain.model.ProjectStatistics
import com.titan.domain.repository.ProjectRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProjectRepositoryImpl @Inject constructor(
    private val projectDao: ProjectDao,
) : ProjectRepository {

    override fun getAllProjects(): Flow<List<Project>> =
        projectDao.getAllProjects().map { list -> list.map { it.toDomain() } }

    override fun getRecentProjects(limit: Int): Flow<List<Project>> =
        projectDao.getRecentProjects(limit).map { list -> list.map { it.toDomain() } }

    override fun getPinnedProjects(): Flow<List<Project>> =
        projectDao.getPinnedProjects().map { list -> list.map { it.toDomain() } }

    override fun getProjectById(id: String): Flow<Project?> =
        projectDao.getProjectById(id).map { it?.toDomain() }

    override fun getProjectByPath(path: String): Flow<Project?> =
        projectDao.getProjectByPath(path).map { it?.toDomain() }

    override suspend fun createProject(project: Project): Result<Project> {
        return try {
            val projectDir = File(project.path)
            if (!projectDir.exists()) {
                projectDir.mkdirs()
            }
            createProjectTemplate(project)
            val entity = project.toEntity()
            projectDao.insertProject(entity)
            Result.success(project)
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override suspend fun updateProject(project: Project): Result<Project> {
        return try {
            projectDao.updateProject(project.toEntity())
            Result.success(project)
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override suspend fun deleteProject(id: String): Result<Unit> {
        return try {
            val project = projectDao.getProjectById(id).first() ?: return Result.error(IllegalArgumentException("Project not found"))
            val projectDir = File(project.path)
            if (projectDir.exists()) {
                projectDir.deleteRecursively()
            }
            projectDao.deleteById(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override suspend fun renameProject(id: String, newName: String): Result<Project> {
        return try {
            val entity = projectDao.getProjectById(id).first() ?: return Result.error(IllegalArgumentException("Project not found"))
            val oldDir = File(entity.path)
            val newDir = File(oldDir.parentFile, newName)
            if (oldDir.exists()) {
                oldDir.renameTo(newDir)
            }
            val updated = entity.copy(name = newName, path = newDir.absolutePath, updatedAt = System.currentTimeMillis())
            projectDao.updateProject(updated)
            Result.success(updated.toDomain())
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override suspend fun duplicateProject(id: String): Result<Project> {
        return try {
            val entity = projectDao.getProjectById(id).first() ?: return Result.error(IllegalArgumentException("Project not found"))
            val sourceDir = File(entity.path)
            val newName = "${entity.name}_copy"
            val destDir = File(sourceDir.parentFile, newName)
            sourceDir.copyRecursively(destDir)
            val newProject = entity.copy(
                id = java.util.UUID.randomUUID().toString(),
                name = newName,
                path = destDir.absolutePath,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                lastOpenedAt = System.currentTimeMillis(),
                isPinned = false,
            )
            projectDao.insertProject(newProject)
            Result.success(newProject.toDomain())
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override suspend fun togglePinProject(id: String): Result<Unit> {
        return try {
            projectDao.togglePin(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override suspend fun importProjectFromZip(zipPath: String, destinationPath: String): Result<Project> {
        return try {
            val destDir = File(destinationPath)
            destDir.mkdirs()
            ZipInputStream(java.io.FileInputStream(zipPath)).use { zis ->
                var entry: ZipEntry? = zis.nextEntry
                while (entry != null) {
                    val file = File(destDir, entry.name)
                    if (entry.isDirectory) {
                        file.mkdirs()
                    } else {
                        file.parentFile?.mkdirs()
                        file.outputStream().use { out -> zis.copyTo(out) }
                    }
                    zis.closeEntry()
                    entry = zis.nextEntry
                }
            }
            val project = Project(
                name = destDir.name,
                path = destDir.absolutePath,
            )
            projectDao.insertProject(project.toEntity())
            Result.success(project)
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override suspend fun exportProjectToZip(projectId: String, outputPath: String): Result<String> {
        return try {
            val entity = projectDao.getProjectById(projectId).first() ?: return Result.error(IllegalArgumentException("Project not found"))
            val sourceDir = File(entity.path)
            ZipOutputStream(java.io.FileOutputStream(outputPath)).use { zos ->
                sourceDir.walkTopDown().forEach { file ->
                    val entryName = file.relativeTo(sourceDir).path
                    if (file.isFile) {
                        zos.putNextEntry(ZipEntry(entryName))
                        file.inputStream().use { it.copyTo(zos) }
                        zos.closeEntry()
                    } else if (file.isDirectory && entryName.isNotEmpty()) {
                        zos.putNextEntry(ZipEntry("$entryName/"))
                        zos.closeEntry()
                    }
                }
            }
            Result.success(outputPath)
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override suspend fun cloneGitRepository(url: String, destinationPath: String, branch: String?): Result<Project> {
        return try {
            val dir = File(destinationPath)
            if (!dir.exists()) dir.mkdirs()
            val name = url.substringAfterLast("/").removeSuffix(".git")
            val project = Project(
                name = name,
                path = dir.absolutePath,
            )
            projectDao.insertProject(project.toEntity())
            Result.success(project)
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    override suspend fun getProjectStatistics(id: String): Result<ProjectStatistics> {
        return try {
            val entity = projectDao.getProjectById(id).first() ?: return Result.error(IllegalArgumentException("Project not found"))
            val projectDir = File(entity.path)
            var totalFiles = 0
            var totalLines = 0
            var totalSize = 0L
            var kotlinFiles = 0
            var javaFiles = 0
            var xmlFiles = 0
            var gradleFiles = 0
            var otherFiles = 0

            projectDir.walkTopDown().filter { it.isFile }.forEach { file ->
                totalFiles++
                totalSize += file.length()
                val ext = file.extension.lowercase()
                when {
                    ext == "kt" || ext == "kts" -> {
                        kotlinFiles++
                        totalLines += file.readLines().size
                    }
                    ext == "java" -> {
                        javaFiles++
                        totalLines += file.readLines().size
                    }
                    ext == "xml" -> xmlFiles++
                    ext == "gradle" || ext == "gradle.kts" -> gradleFiles++
                    else -> otherFiles++
                }
            }

            Result.success(ProjectStatistics(totalFiles, totalLines, totalSize, kotlinFiles, javaFiles, xmlFiles, gradleFiles, otherFiles))
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    private fun createProjectTemplate(project: Project) {
        val dir = File(project.path)
        when (project.templateType) {
            com.titan.domain.model.TemplateType.EMPTY -> createEmptyProject(dir, project)
            com.titan.domain.model.TemplateType.KOTLIN -> createKotlinProject(dir, project)
            com.titan.domain.model.TemplateType.COMPOSE -> createComposeProject(dir, project)
            com.titan.domain.model.TemplateType.MVVM -> createMVVMProject(dir, project)
            com.titan.domain.model.TemplateType.CLEAN_ARCHITECTURE -> createCleanArchitectureProject(dir, project)
            com.titan.domain.model.TemplateType.JAVA -> createJavaProject(dir, project)
            com.titan.domain.model.TemplateType.LIBRARY -> createEmptyProject(dir, project)
            com.titan.domain.model.TemplateType.NAVIGATION -> createComposeProject(dir, project)
            com.titan.domain.model.TemplateType.ROOM -> createRoomProject(dir, project)
            com.titan.domain.model.TemplateType.RETROFIT -> createRetrofitProject(dir, project)
            com.titan.domain.model.TemplateType.HILT -> createHiltProject(dir, project)
        }
    }

    private fun createEmptyProject(dir: File, project: Project) {
        File(dir, "README.md").writeText("# ${project.name}\n\n${project.description}")
        File(dir, ".gitignore").writeText("*.iml\n.gradle\n/local.properties\n/.idea\n/build\n/captures\n.externalNativeBuild\n.cxx\n")
    }

    private fun createKotlinProject(dir: File, project: Project) {
        createEmptyProject(dir, project)
        val pkg = project.packageName.ifEmpty { "com.example.${project.name.lowercase().replace(' ', '_')}" }
        val pkgPath = pkg.replace('.', '/')
        val srcDir = File(dir, "src/main/java/$pkgPath")
        srcDir.mkdirs()
        File(srcDir, "MainActivity.kt").writeText("""package $pkg

import android.app.Activity
import android.os.Bundle

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}""")
        File(dir, "src/main/AndroidManifest.xml").writeText("""<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="$pkg">
    <application
        android:allowBackup="true"
        android:label="${project.name}"
        android:supportsRtl="true"
        android:theme="@style/Theme.AppCompat.Light.DarkActionBar">
        <activity android:name=".MainActivity" android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>""")
        val gradleExt = if (project.buildSystem == com.titan.domain.model.BuildSystem.GRADLE_KTS) "gradle.kts" else "gradle"
        File(dir, "build.$gradleExt").writeText(createGradleBuildFile(project, pkg, gradleExt))
        File(dir, "settings.$gradleExt").writeText("pluginManagement {\n    repositories {\n        google()\n        mavenCentral()\n        gradlePluginPortal()\n    }\n}\nrootProject.name = \"${project.name}\"\ninclude(\":app\")\n")
    }

    private fun createComposeProject(dir: File, project: Project) {
        createKotlinProject(dir, project)
        val pkg = project.packageName.ifEmpty { "com.example.${project.name.lowercase().replace(' ', '_')}" }
        val pkgPath = pkg.replace('.', '/')
        val srcDir = File(dir, "src/main/java/$pkgPath")
        File(srcDir, "MainActivity.kt").writeText("""package $pkg

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface {
                    Greeting(name = "${project.name}")
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Welcome to ${project.name}!", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MaterialTheme {
        Greeting("${project.name}")
    }
}""")
    }

    private fun createMVVMProject(dir: File, project: Project) { createComposeProject(dir, project) }
    private fun createCleanArchitectureProject(dir: File, project: Project) {
        val pkg = project.packageName.ifEmpty { "com.example.${project.name.lowercase().replace(' ', '_')}" }
        listOf("domain", "data", "presentation").forEach { layer ->
            File(dir, "src/main/java/${pkg.replace('.', '/')}/$layer").mkdirs()
        }
        createComposeProject(dir, project)
    }
    private fun createJavaProject(dir: File, project: Project) { createKotlinProject(dir, project) }
    private fun createRoomProject(dir: File, project: Project) { createComposeProject(dir, project) }
    private fun createRetrofitProject(dir: File, project: Project) { createComposeProject(dir, project) }
    private fun createHiltProject(dir: File, project: Project) { createComposeProject(dir, project) }

    private fun createGradleBuildFile(project: Project, pkg: String, ext: String): String {
        return if (ext == "gradle.kts") {
            """plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android") version "1.9.24"
    kotlin("plugin.serialization") version "1.9.24"
}

android {
    namespace = "$pkg"
    compileSdk = 35

    defaultConfig {
        applicationId = "$pkg"
        minSdk = ${project.minSdk}
        targetSdk = ${project.targetSdk}
        versionCode = ${project.versionCode}
        versionName = "${project.versionName}"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.activity:activity-compose:1.9.1")
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
}"""
        } else {
            """plugins {
    id 'com.android.application'
    id 'org.jetbrains.kotlin.android'
}

android {
    namespace '$pkg'
    compileSdk 35

    defaultConfig {
        applicationId "$pkg"
        minSdk ${project.minSdk}
        targetSdk ${project.targetSdk}
        versionCode ${project.versionCode}
        versionName "${project.versionName}"
    }

    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_17
        targetCompatibility JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = '17'
    }
}

dependencies {
    implementation 'androidx.core:core-ktx:1.13.1'
    implementation 'androidx.appcompat:appcompat:1.7.0'
    implementation 'com.google.android.material:material:1.12.0'
}"""
        }
    }

    private fun ProjectEntity.toDomain(): Project = Project(
        id = id, name = name, path = path, description = description,
        createdAt = createdAt, updatedAt = updatedAt, lastOpenedAt = lastOpenedAt,
        isPinned = isPinned,
        templateType = com.titan.domain.model.TemplateType.entries.firstOrNull { it.name == templateType } ?: com.titan.domain.model.TemplateType.EMPTY,
        buildSystem = com.titan.domain.model.BuildSystem.entries.firstOrNull { it.name == buildSystem } ?: com.titan.domain.model.BuildSystem.GRADLE_KTS,
        minSdk = minSdk, targetSdk = targetSdk, packageName = packageName,
        versionName = versionName, versionCode = versionCode,
        language = com.titan.domain.model.ProgrammingLanguage.entries.firstOrNull { it.name == language } ?: com.titan.domain.model.ProgrammingLanguage.KOTLIN,
        isOpened = isOpened, sizeInBytes = sizeInBytes, fileCount = fileCount,
    )

    private fun Project.toEntity(): ProjectEntity = ProjectEntity(
        id = id, name = name, path = path, description = description,
        createdAt = createdAt, updatedAt = updatedAt, lastOpenedAt = lastOpenedAt,
        isPinned = isPinned, templateType = templateType.name,
        buildSystem = buildSystem.name, minSdk = minSdk, targetSdk = targetSdk,
        packageName = packageName, versionName = versionName, versionCode = versionCode,
        language = language.name, isOpened = isOpened, sizeInBytes = sizeInBytes, fileCount = fileCount,
    )
}