
import io.izzel.taboolib.gradle.*
import org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_1_8
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


plugins {
    java
    id("io.izzel.taboolib") version "2.0.27"
    id("org.jetbrains.kotlin.jvm") version "2.2.0"
}

taboolib {
    env {
        install(Basic)
        install(I18n)
        install(Metrics)
        install(MinecraftChat)
        install(CommandHelper)
        install(Database)
        install(Bukkit)
        install(BukkitUtil)
    }
    description {
        name = "BilibiliVideoQRCodeOneBot"
        desc("BilibiliVideo OneBot二维码适配器")
        contributors {
            name("BingZi-233")
        }
        dependencies {
            name("OneBot")
            name("BilibiliVideo")
        }
    }
    version { taboolib = "6.2.3-2eb93b5" }
}

repositories {
    mavenCentral()
    maven("https://repo.aeoliancloud.com/repository/releases/")
}

dependencies {
    compileOnly("ink.ptms.core:v12004:12004:mapped")
    compileOnly("ink.ptms.core:v12004:12004:universal")
    compileOnly(kotlin("stdlib"))
    compileOnly(fileTree("libs"))
    compileOnly("online.bingzi:onebot:1.2.0-65732a8")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        jvmTarget.set(JVM_1_8)
        freeCompilerArgs.add("-Xjvm-default=all")
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}