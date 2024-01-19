import jetbrains.buildServer.configs.kotlin.v2019_2.*
import jetbrains.buildServer.configs.kotlin.v2019_2.projectFeatures.githubIssues

/*
The settings script is an entry point for defining a TeamCity
project hierarchy. The script should contain a single call to the
project() function with a Project instance or an init function as
an argument.
VcsRoots, BuildTypes, Templates, and subprojects can be
registered inside the project using the vcsRoot(), buildType(),
template(), and subProject() methods respectively.
To debug settings scripts in command-line, run the
    mvnDebug org.jetbrains.teamcity:teamcity-configs-maven-plugin:generate
command and attach your debugger to the port 8000.
To debug in IntelliJ Idea, open the 'Maven Projects' tool window (View
-> Tool Windows -> Maven Projects), find the generate task node
(Plugins -> teamcity-configs -> teamcity-configs:generate), the
'Debug' option is available in the context menu for the task.
*/

version = "2021.2"

project {
    description = "XML Driven UI Library"

    params {
        param("env.JDK_VERSION", "jdk17")
        param("Current Minecraft Version", "main")
        text("Repository", "ldtteam/blockUI", label = "Repository", description = "The repository for BlockUI.", readOnly = true, allowEmpty = true)
        param("env.Version.Minor", "0")
        param("env.Version.Patch", "0")
        param("Upsource.Project.Id", "blockui")
        param("env.Version.Suffix", "")
        param("env.Version.Major", "1")
        param("env.GRADLE_VERSION", "7.3")
        param("Default.Branch", "version/main")
        param("Project.Type", "mods")
        param("filename.prefix", "blockui")

        text("env.Version", "%env.Version.Major%.%env.Version.Minor%.%env.Version.Patch%%env.Version.Suffix%", label = "Version", description = "The version of the project.", display = ParameterDisplay.HIDDEN, allowEmpty = true)
    }

    features {
        githubIssues {
            id = "PROJECT_EXT_35"
            displayName = "ldtteam/blockui"
            repositoryURL = "https://github.com/ldtteam/blockui"
            authType = accessToken {
                accessToken = "credentialsJSON:47381468-aceb-4992-93c9-1ccd4d7aa67f"
            }
        }
    }
    subProjectsOrder = arrayListOf(RelativeId("Release"), RelativeId("UpgradeBetaRelease"), RelativeId("Beta"), RelativeId("OfficialPublications"), RelativeId("Branches"), RelativeId("PullRequests2"))

    subProject(UpgradeBetaRelease)
    subProject(Beta)
    subProject(PullRequests2)
    subProject(Branches)
    subProject(OfficialPublications)
    subProject(Release)
}


object Beta : Project({
    name = "Beta"
    description = "Beta version builds of BlockUI"

    buildType(Beta_Release)

    params {
        param("Default.Branch", "version/%Current Minecraft Version%")
        param("VCS.Branches", "+:refs/heads/version/(*)")
        param("env.CURSERELEASETYPE", "beta")
        param("env.Version.Suffix", "-BETA")
    }
})

object Beta_Release : BuildType({
    templates(AbsoluteId("LetSDevTogether_BuildWithRelease"))
    name = "Release"
    description = "Releases the mod as Beta to CurseForge"

    params {
        param("Project.Type", "mods")
        param("env.Version.Patch", "${OfficialPublications_CommonB.depParamRefs.buildNumber}")
    }

    vcs {
        branchFilter = "+:*"
    }

    dependencies {
        snapshot(OfficialPublications_CommonB) {
            reuseBuilds = ReuseBuilds.NO
            onDependencyFailure = FailureAction.FAIL_TO_START
        }
    }
})


object Branches : Project({
    name = "Branches"
    description = "All none release branches."

    buildType(Branches_Common)
    buildType(Branches_Build)

    params {
        text("Default.Branch", "CI/Default", label = "Default branch", description = "The default branch for branch builds", readOnly = true, allowEmpty = true)
        param("VCS.Branches", """
            +:refs/heads/(*)
            -:refs/heads/version/*
            -:refs/heads/testing/*
            -:refs/heads/release/*
            -:refs/pull/*/head
            -:refs/heads/CI/*
        """.trimIndent())
        param("env.Version.Suffix", "-PERSONAL")
    }

    cleanup {
        baseRule {
            all(days = 60)
        }
    }
})

object Branches_Build : BuildType({
    templates(AbsoluteId("LetSDevTogether_Build"))
    name = "Build"
    description = "Builds the branch without testing."

    params {
        param("Project.Type", "mods")
        param("env.Version.Patch", "${Branches_Common.depParamRefs.buildNumber}")
    }

    dependencies {
        snapshot(Branches_Common) {
            reuseBuilds = ReuseBuilds.NO
            onDependencyFailure = FailureAction.FAIL_TO_START
        }
    }
})

object Branches_Common : BuildType({
    templates(AbsoluteId("LetSDevTogether_CommonBuildCounter"))
    name = "Common Build Counter"
    description = "Tracks the amount of builds run for branches"
})


object OfficialPublications : Project({
    name = "Official Publications"
    description = "Holds projects and builds related to official publications"

    buildType(OfficialPublications_CommonB)
})

object OfficialPublications_CommonB : BuildType({
    templates(AbsoluteId("LetSDevTogether_CommonBuildCounter"))
    name = "Common Build Counter"
    description = "Represents the version counter within Minecolonies for official releases."
})


object PullRequests2 : Project({
    name = "Pull Requests"
    description = "All open pull requests"

    buildType(PullRequests2BuildAndTest)
    buildType(PullRequests2CommonBuildCounter)

    params {
        text("Default.Branch", "CI/Default", label = "Default branch", description = "The default branch for pull requests.", readOnly = true, allowEmpty = false)
        param("VCS.Branches", """
            -:refs/heads/*
            +:refs/pull/(*)/head
            -:refs/heads/(CI/*)
        """.trimIndent())
        text("env.Version", "%env.Version.Major%.%env.Version.Minor%.%build.counter%-PR", label = "Version", description = "The version of the project.", display = ParameterDisplay.HIDDEN, allowEmpty = true)
    }

    cleanup {
        baseRule {
            all(days = 60)
        }
    }
})

object PullRequests2BuildAndTest : BuildType({
    templates(AbsoluteId("LetSDevTogether_BuildWithTesting"))
    name = "Build and Test"
    description = "Builds and Tests the pull request."

    artifactRules = """
        +:build\libs\*.jar => build\libs
        +:build\distributions\mods-*.zip => build\distributions
    """.trimIndent()

    params {
        param("env.Version.Patch", "${PullRequests2CommonBuildCounter.depParamRefs.buildNumber}")
        param("env.Version.Suffix", "-PR")
    }

    features {
        feature {
            id = "com.ldtteam.teamcity.github.commenting.GithubCommentingBuildFeature"
            type = "com.ldtteam.teamcity.github.commenting.GithubCommentingBuildFeature"
            param("privateKey", "-----")
            param("appId", "154983")
            param("branch", "%teamcity.build.branch%")
        }
    }

    dependencies {
        snapshot(PullRequests2CommonBuildCounter) {
            onDependencyFailure = FailureAction.FAIL_TO_START
        }
    }

    disableSettings("BUILD_EXT_15")
})


object PullRequests2CommonBuildCounter : BuildType({
    templates(AbsoluteId("LetSDevTogether_CommonBuildCounter"))
    name = "Common Build Counter"
    description = "Defines version numbers uniquely over all Pull Request builds"
})


object Release : Project({
    name = "Release"
    description = "Beta version builds of BlockUI"

    buildType(Release_Release)

    params {
        param("Default.Branch", "release/%Current Minecraft Version%")
        param("VCS.Branches", "+:refs/heads/release/(*)")
        param("env.CURSERELEASETYPE", "release")
        param("env.Version.Suffix", "-RELEASE")
    }
})

object Release_Release : BuildType({
    templates(AbsoluteId("LetSDevTogether_BuildWithRelease"))
    name = "Release"
    description = "Releases the mod as Release to CurseForge"

    params {
        param("Project.Type", "mods")
        param("env.Version.Patch", "${OfficialPublications_CommonB.depParamRefs.buildNumber}")
    }

    dependencies {
        snapshot(OfficialPublications_CommonB) {
            reuseBuilds = ReuseBuilds.NO
            onDependencyFailure = FailureAction.FAIL_TO_START
        }
    }
})

object UpgradeBetaRelease : Project({
    name = "Upgrade Beta -> Release"
    description = "Upgrades the current Beta to Release"

    buildType(UpgradeBetaRelease_UpgradeBetaRelease)
})

object UpgradeBetaRelease_UpgradeBetaRelease : BuildType({
    templates(AbsoluteId("LetSDevTogether_Upgrade"))
    name = "Upgrade Beta -> Release"
    description = "Upgrades the current Beta to Release."

    params {
        text("Source.Branch", "version", label = "Source branch type", description = "The source branch type for the upgrade. EG: version or testing", allowEmpty = false)
        text("Default.Branch", "release/%Current Minecraft Version%", label = "Default branch", description = "The default branch of this build.", allowEmpty = true)
        param("VCS.Branches", "+:refs/heads/release/(*)")
        text("Target.Branch", "release", label = "Target branch type", description = "The target branch type for the upgrade. EG: testing or release.", allowEmpty = false)
        text("env.Version", "%env.Version.Major%.%env.Version.Minor%.%build.counter%-RELEASE", label = "Version", description = "The version of the project.", display = ParameterDisplay.HIDDEN, allowEmpty = true)
    }
})