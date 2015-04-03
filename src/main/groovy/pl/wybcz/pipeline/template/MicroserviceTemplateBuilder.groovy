package pl.wybcz.pipeline.template

import javaposse.jobdsl.dsl.DslFactory
import pl.wybcz.pipeline.domain.GitProject
import pl.wybcz.pipeline.pr.PrBuilder
import pl.wybcz.pipeline.pr.GithubPrBuilder
import pl.wybcz.pipeline.pr.StashPrBuilder

class MicroserviceTemplateBuilder {

    DslFactory dslFactory
    PrBuilder prBuilder
    List<GitProject> projects

    static void pipeline(DslFactory dslFactory, @DelegatesTo(MicroserviceTemplateBuilder) Closure closure) {
        MicroserviceTemplateBuilder microserviceTemplateBuilder = new MicroserviceTemplateBuilder(
                dslFactory: dslFactory
        )
        closure.delegate = microserviceTemplateBuilder
        closure.call()
    }

    void buildGithubPrs(@DelegatesTo(GithubPrBuilder) closure) {
        GithubPrBuilder githubPrBuilder = new GithubPrBuilder(
                dslFactory: dslFactory
        )
        closure.delegate = githubPrBuilder
        this.prBuilder = githubPrBuilder
        closure.call()
    }

    void buildStashPrs(@DelegatesTo(StashPrBuilder) closure) {
        StashPrBuilder stashPrBuilder = new StashPrBuilder(
                dslFactory: dslFactory
        )
        closure.delegate = stashPrBuilder
        this.prBuilder = stashPrBuilder
        closure.call()
    }

    void forProjects(List<GitProject> projects) {
        this.projects = projects
    }

    void buildJobs() {
        MicroserviceJobsBuilder microserviceJobsBuilder = new MicroserviceJobsBuilder(dslFactory, prBuilder)
        projects.each {
            microserviceJobsBuilder.buildJobs(it.name, it.cloneUrl)
        }
    }

    void buildViews() {
        Map<String, List<String>> realmMultimap = new SuffixRealmParser().convertToRealmMultimap(projects)
        realmMultimap.each { String realm, List<String> projects ->
            new MicroserviceViewsBuilder(dslFactory).buildViews(realm, projects)
        }
    }

    void buildViews(RealmConverter realmConverter) {
        realmConverter.convertToRealmMultimap(projects).each { String realm, List<String> projects ->
            new MicroserviceViewsBuilder(dslFactory).buildViews(realm, projects)
        }
    }
}