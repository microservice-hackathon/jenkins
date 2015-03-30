package pl.wybcz

import groovy.xml.XmlUtil
import org.custommonkey.xmlunit.DetailedDiff
import org.custommonkey.xmlunit.Diff
import org.custommonkey.xmlunit.ElementNameAndAttributeQualifier
import org.custommonkey.xmlunit.XMLUnit

trait XmlComparator {

    void compareXmls(String file, Node nodeToCompare) {
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