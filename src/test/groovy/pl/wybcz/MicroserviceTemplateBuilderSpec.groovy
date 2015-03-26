package pl.wybcz

import groovy.xml.XmlUtil
import javaposse.jobdsl.dsl.Job
import javaposse.jobdsl.dsl.JobParent
import org.custommonkey.xmlunit.DetailedDiff
import org.custommonkey.xmlunit.Diff
import org.custommonkey.xmlunit.ElementNameAndAttributeQualifier
import org.custommonkey.xmlunit.XMLUnit
import spock.lang.Specification

class MicroserviceTemplateBuilderSpec extends Specification implements JobSpecTrait {

    private static final String JOB_NAME = 'test-job'

    JobParent jobParent = createJobParent()

    void 'test XML output'() {
        given:
            MicroserviceTemplateBuilder builder = new MicroserviceTemplateBuilder(
                projectName: JOB_NAME,
                projectGitRepo: 'git@github.com:example/example.git'
            )

        when:
            List<Job> jobs = builder.build(jobParent)

        then:
            jobs.each {
                assertThatStepIsOk(it)
            }
    }

    void assertThatStepIsOk(Job job) {
        String fileName = job.name - "$JOB_NAME-"
        String referenceXml = XmlUtil.serialize(new File(MicroserviceTemplateBuilderSpec.getResource("/microservice/${fileName}.xml").file).text.stripIndent().stripMargin())
        String nodeXml = XmlUtil.serialize(job.node).stripIndent().stripMargin()
        Diff diff = XMLUnit.compareXML(referenceXml, nodeXml)
        XMLUnit.setIgnoreWhitespace(true)
        diff.overrideElementQualifier(new ElementNameAndAttributeQualifier())
        if (!diff.similar()) {
            DetailedDiff detailedDiff = new DetailedDiff(diff)
            throw new XmlsAreNotSimillar(job.name, detailedDiff.allDifferences)
        }
    }


    static class XmlsAreNotSimillar extends RuntimeException {
        XmlsAreNotSimillar(String projectName, List diffs) {
            super("For project [$projectName] the following differences where found [$diffs]")
        }
    }
}
