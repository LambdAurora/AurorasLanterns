import dev.lambdaurora.mcdev.api.EnvironmentType
import dev.lambdaurora.mcdev.api.McVersionLookup
import dev.lambdaurora.mcdev.api.ModUtils
import dev.lambdaurora.mcdev.api.ModVersionDependency
import dev.lambdaurora.mcdev.api.manifest.MixinEntry
import dev.lambdaurora.mcdev.api.manifest.ModEnvironment
import dev.lambdaurora.mcdev.task.ConvertAccessWidenerToTransformer
import dev.lambdaurora.mcdev.task.GenerateNeoForgeJiJDataTask
import dev.lambdaurora.mcdev.task.packaging.PackageModrinthTask

plugins {
	alias(libs.plugins.loom)
	alias(libs.plugins.lambdamcdev)
	alias(libs.plugins.licenser)
	`java-library`
	`maven-publish`
}

lambdamcdev.namespace.set(project.property("mod_namespace") as String)
base.archivesName.set(lambdamcdev.namespace)

val mcVersion = libs.versions.minecraft.get()
val compatibleMcVersions: Set<String> = setOf()
val VERSION = project.property("mod_version") as String
val supportNeoforge = (project.property("support_neoforge") as String).toBoolean()
version = "$VERSION+$mcVersion"

// This field defines the Java version your mod target.
val targetJavaVersion = Integer.parseInt(project.property("java_version").toString())

repositories {
	mavenCentral()
	exclusiveContent {
		forRepository {
			maven {
				name = "Gegy"
				url = uri("https://maven.gegy.dev/releases/")
			}
		}
		filter {
			includeGroupAndSubgroups("dev.lambdaurora")
		}
	}
}

loom {
	accessWidenerPath = file("src/main/resources/auroraslanterns.classtweaker")
	splitEnvironmentSourceSets()
	mixin {
		useLegacyMixinAp = false
	}
}

fabricApi {
	configureDataGeneration {
		client = true
	}
	configureTests {
		eula = true
	}
}

dependencies {
	minecraft(libs.minecraft)
	implementation(libs.fabric.loader)
	implementation(libs.fabric.api)

	implementation(libs.yumi.mc.foundation)
	include(libs.yumi.mc.foundation)
}

java {
	sourceCompatibility = JavaVersion.toVersion(targetJavaVersion)
	targetCompatibility = JavaVersion.toVersion(targetJavaVersion)

	withSourcesJar()
}

lambdamcdev {
	manifests {
		fmj {
			val sourcesLink = "https://github.com/LambdAurora/AurorasLanterns"

			withName(project.property("mod_name") as String)
			withDescription(project.property("mod_description") as String)
			withAuthors("LambdAurora")
			withContact {
				it.withHomepage("https://lambdaurora.dev/projects/auroraslanterns")
					.withSources("$sourcesLink.git")
					.withIssues("$sourcesLink/issues")
			}
			withLicense("Lambda License")
			withIcon("assets/${namespace.get()}/icon.png")
			withEnvironment("*")
			withEntrypoints("yumi:init", "dev.lambdaurora.auroraslanterns.AurorasLanterns")
			withEntrypoints("yumi:client_init", "dev.lambdaurora.auroraslanterns.client.AurorasLanternsClient")
			withEntrypoints("fabric-datagen", "dev.lambdaurora.auroraslanterns.client.resource.datagen.AurorasLanternsStaticDatagen")
			withEntrypoints(
				"fabric-gametest",
				"dev.lambdaurora.auroraslanterns.test.RedstoneLanternTest",
				"dev.lambdaurora.auroraslanterns.test.WallLanternTest",
			)
			withAccessWidener("${namespace.get()}.classtweaker")
			withMixins(
				MixinEntry("${namespace.get()}.mixins.json"),
				MixinEntry("${namespace.get()}.client.mixins.json", ModEnvironment.CLIENT),
			)
			withDepend("fabricloader", ">=${libs.versions.fabric.loader.get()}")
			withDepend("minecraft", project.property("fabric_mc_constraints").toString())
			withDepend("java", ">=$targetJavaVersion")
			withDepend("yumi_mc_core", ">=${libs.versions.yumi.mc.foundation.get()}")
			withDepend("fabric-api", ">=${libs.versions.fabric.api.get()}")
			withModMenu {
				it.withCurseForge("https://www.curseforge.com/minecraft/mc-mods/auroraslanterns")
					.withDiscord("https://discord.lambdaurora.dev/")
					.withGitHubReleases("$sourcesLink/releases")
					.withModrinth("https://modrinth.com/mod/auroraslanterns")
					.withLink("modmenu.bluesky", "https://bsky.app/profile/lambdaurora.dev")
					.withLink("modmenu.donate", "https://donate.lambdaurora.dev/")
			}
		}

		if (supportNeoforge) {
			val fmj = this.fmj().get()

			nmt {
				fmj.copyTo(this)
				withLoaderVersion("[2,)")
				withBlurIcon(false)
				withYumiEntrypoints("yumi:init", "dev.lambdaurora.auroraslanterns.AurorasLanterns")
				withYumiEntrypoints("yumi:client_init", "dev.lambdaurora.auroraslanterns.client.AurorasLanternsClient")
				withAccessTransformer("META-INF/accesstransformer.cfg")
				withMixins("${namespace.get()}.mixins.json", "${namespace.get()}.client.mixins.json")
				withDepend("minecraft", project.property("neoforge_mc_constraints").toString())
				withDepend("yumi_mc_core", "[${libs.versions.yumi.mc.foundation.get()},)")
				withDepend("fabric_api", "[${libs.versions.fabric.api.get()},)")
			}
		}
	}

	setupActionsRefCheck()
}

tasks.withType<JavaCompile>().configureEach {
	options.encoding = "UTF-8"
	options.isDeprecation = true
	options.isIncremental = true
	options.release.set(targetJavaVersion)
}

tasks.processResources {
	inputs.property("version", project.version)

	filesMatching("fabric.mod.json") {
		expand("version" to (inputs.properties["version"] as String))
	}

	exclude(".cache/**")
}

tasks.jar {
	inputs.property("archivesName", base.archivesName)

	from("LICENSE") {
		rename { "${it}_${inputs.properties["archivesName"]}" }
	}
}

license {
	rule(file("metadata/HEADER"))

	include("**/*.java")
}

val convertAWtoATTask = tasks.register("convertAWtoAT", ConvertAccessWidenerToTransformer::class) {
	this.group = "generation"
	this.enabled = supportNeoforge
	this.input = loom.accessWidenerPath
	this.output = project.layout.buildDirectory.get().file("generated/accesstransformer.cfg")
}

val generateJarJarMetadataTask = tasks.register<GenerateNeoForgeJiJDataTask>("generateJarJarMetadata") {
	this.enabled = supportNeoforge
	val includeConfig = project.configurations.getByName("includeInternal");
	this.from(includeConfig)
	this.outputFile.set(
		project.layout.buildDirectory
			.asFile
			.map(File::toPath)
			.map { path -> path.resolve("generated/jarjar/metadata.json").toFile() }
			.get()
	)
}

if (supportNeoforge) {
	tasks.named<Jar>("jar") {
		from(generateJarJarMetadataTask.map { it.outputFile }) {
			into("META-INF/jarjar")
		}
		from(convertAWtoATTask) {
			into("META-INF")
		}
	}

	tasks.named<Jar>("sourcesJar") {
		this.from(convertAWtoATTask) {
			into("META-INF")
		}
	}
}

val README = ModUtils.parseReadme(
	project, "https://raw.githubusercontent.com/LambdAurora/AurorasLanterns/26.1/\$2"
)
val CHANGELOG_CONTENT = ModUtils.fetchChangelog(project, VERSION)

tasks.register<PackageModrinthTask>("packageModrinth") {
	this.group = "publishing"
	this.versionType.set(ModUtils.getVersionType(VERSION, mcVersion))
	this.versionName.set("${project.property("mod_name")} $VERSION (${McVersionLookup.getVersionTag(mcVersion)})")
	this.gameVersions.set(listOf(mcVersion) + compatibleMcVersions)
	this.loaders.set(listOf("fabric", "quilt"))
	if (supportNeoforge) {
		this.loaders.add("neoforge")
	}
	this.environment.set(EnvironmentType.CLIENT_AND_SERVER)
	this.dependencies.set(
		listOf(
			ModVersionDependency("P7dR8mSH", ModVersionDependency.Type.REQUIRED), // Fabric API
		)
	)
	this.changelog.set(CHANGELOG_CONTENT)
	this.readme.set(README)
	this.files.setFrom(tasks.jar.get())
}

publishing {
	publications {
		create<MavenPublication>("mavenJava") {
			from(components["java"])

			pom {
				name.set(project.property("mod_name") as String)
				description.set(project.property("mod_description") as String)
			}
		}
	}

	repositories {
		mavenLocal()
	}
}
