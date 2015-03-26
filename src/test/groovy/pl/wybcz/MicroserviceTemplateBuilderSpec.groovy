package pl.wybcz

import groovy.xml.XmlUtil
import javaposse.jobdsl.dsl.Job
import javaposse.jobdsl.dsl.JobParent
import javaposse.jobdsl.dsl.View
import org.custommonkey.xmlunit.DetailedDiff
import org.custommonkey.xmlunit.Diff
import org.custommonkey.xmlunit.ElementNameAndAttributeQualifier
import org.custommonkey.xmlunit.XMLUnit
import spock.lang.Specification

class MicroserviceTemplateBuilderSpec extends Specification implements JobSpecTrait {

    private static final String JOB_NAME = 'test-job'

    JobParent jobParent = createJobParent()

    void 'test XML output for jobs'() {
        given:
            MicroserviceTemplateBuilder builder = new MicroserviceTemplateBuilder(
                projectName: JOB_NAME,
                projectGitRepo: 'git@github.com:example/example.git'
            )

        when:
            List<Job> jobs = builder.buildJobs(jobParent)

        then:
            jobs.each {
                assertThatJobIsOk(it)
            }
    }

    void 'test XML output for views'() {
        given:
            MicroserviceTemplateBuilder builder = new MicroserviceTemplateBuilder(
                realm: 'waw',
                projects: ['build-waw', 'publish-waw']
            )

        when:
            List<View> views = builder.buildViews(jobParent)

        then:
            views.each {
                assertThatViewIsOk(it)
            }
    }

    void assertThatJobIsOk(Job job) {
        String fileName = job.name - "$JOB_NAME-"
        compareXmls("/microservice/jobs/${fileName}.xml", job.node)
    }

    void assertThatViewIsOk(View view) {
        String fileName = view.name
        compareXmls("/microservice/views/${fileName}.xml", view.node)
    }

    private static void compareXmls(String file, Node nodeToCompare) {
        String referenceXml = XmlUtil.serialize(new File(MicroserviceTemplateBuilderSpec.getResource(file).file).text.stripIndent().stripMargin())
        String nodeXml = XmlUtil.serialize(nodeToCompare).stripIndent().stripMargin()
        Diff diff = XMLUnit.compareXML(referenceXml, nodeXml)
        XMLUnit.setIgnoreWhitespace(true)
        diff.overrideElementQualifier(new ElementNameAndAttributeQualifier())
        if (!diff.similar()) {
            DetailedDiff detailedDiff = new DetailedDiff(diff)
            throw new XmlsAreNotSimillar(file, detailedDiff.allDifferences)
        }
    }


    static class XmlsAreNotSimillar extends RuntimeException {
        XmlsAreNotSimillar(String file, List diffs) {
            super("For file [$file] the following differences where found [$diffs]")
        }
    }
}
