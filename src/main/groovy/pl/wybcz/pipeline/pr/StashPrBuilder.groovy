package pl.wybcz.pipeline.pr
import groovy.transform.PackageScope
import javaposse.jobdsl.dsl.DslFactory
import javaposse.jobdsl.dsl.Job
import javaposse.jobdsl.dsl.helpers.scm.GitContext

class StashPrBuilder implements PrBuilder {

    @PackageScope DslFactory dslFactory
    @PackageScope String stashHost
    @PackageScope String cronToPollScm
    @PackageScope String mergeStrategy = 'default'
    @PackageScope FastForwardMode fastForwardMode = FastForwardMode.FF
    @PackageScope String username
    @PackageScope String password
    @PackageScope String projectCode
    @PackageScope String repoName

    void stashHost(String stashHost) {
        this.stashHost = stashHost
    }

    void cronToPollScm(String cronToPollScm) {
        this.cronToPollScm = cronToPollScm
    }

    void mergeStrategy(String mergeStrategy) {
        this.mergeStrategy = mergeStrategy
    }

    void fastForwardMode(FastForwardMode fastForwardMode) {
        this.fastForwardMode = fastForwardMode
    }

    void username(String username) {
        this.username = username
    }

    void password(String password) {
        this.password = password
    }

    void projectCode(String projectCode) {
        this.projectCode = projectCode
    }

    void repoName(String repoName) {
        this.repoName = repoName
    }

    @Override
    Job buildPrJob(String projectName) {
        return dslFactory.freeStyleJob("$projectName-pr-build") {
            scm {
                git {
                    remote {
                        name = 'origin'
                        url = "ssh://git@$stashHost/\${projectCode}/\${repositoryName}.git"
                    }
                    branch('*/\${sourceBranch}')
                    mergeOptions('origin', '${targetBranch}')
                    addExtension(delegate as GitContext)
                }
            }
            triggers {
                configure { Node node ->
                    addStashBuildTrigger(node.triggers)
                }
            }
        }
    }

    private void addExtension(GitContext gitContext) {
        def option = gitContext.extensions[0].options[0]
        new Node(option, 'fastForwardMode', fastForwardMode.mode)
    }

    private void addStashBuildTrigger(Node triggers) {
        def node = triggers / 'stashpullrequestbuilder.stashpullrequestbuilder.StashBuildTrigger'
        (node / 'spec').setValue(cronToPollScm)
        (node / 'cron').setValue(cronToPollScm)
        (node / 'stashHost').setValue(stashHost)
        (node / 'username').setValue(username)
        (node / 'password').setValue(password)
        (node / 'projectCode').setValue(projectCode)
        (node / 'repositoryName').setValue(repoName)
        (node / 'checkDestinationCommit').setValue(true)
    }

    enum FastForwardMode {
        FF('FF'), FF_ONLY('FF-ONLY'), NO_FF('NO-FF')

        String mode

        FastForwardMode(String mode) {
            this.mode = mode
        }
    }
}
