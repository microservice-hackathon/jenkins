package pl.wybcz.pipeline.util

import groovy.xml.XmlUtil
import org.custommonkey.xmlunit.DetailedDiff
import org.custommonkey.xmlunit.Diff
import org.custommonkey.xmlunit.ElementNameAndAttributeQualifier
import org.custommonkey.xmlunit.XMLUnit
import pl.wybcz.pipeline.template.MicroserviceTemplateBuilderSpec

trait XmlComparator {

    void compareXmls(String file, Node nodeToCompare) {
        String referenceXml = XmlUtil.serialize(getFileContent(file).stripIndent().stripMargin())
        String nodeXml = XmlUtil.serialize(nodeToCompare).stripIndent().stripMargin()
        Diff diff = XMLUnit.compareXML(referenceXml, nodeXml)
        XMLUnit.setIgnoreWhitespace(true)
        diff.overrideElementQualifier(new ElementNameAndAttributeQualifier())
        if (!diff.similar()) {
            DetailedDiff detailedDiff = new DetailedDiff(diff)
            throw new XmlsAreNotSimilar(file, detailedDiff.allDifferences)
        }
    }

    private static String getFileContent(String file) {
        new File(MicroserviceTemplateBuilderSpec.getResource(file).toURI()).getCanonicalFile().text
    }

    static class XmlsAreNotSimilar extends RuntimeException {
        XmlsAreNotSimilar(String file, List diffs) {
            super("For file [$file] the following differences where found [$diffs]")
        }
    }
}