package pl.wybcz.view

import javaposse.jobdsl.dsl.JobManagement
import javaposse.jobdsl.dsl.View

class Dashboard extends View {

    protected Dashboard(String name, JobManagement jobManagement) {
        super(jobManagement)
        this.name = name
    }

    @Override
    Node getNode() {
        return new XmlParser().parse(this.class.getResourceAsStream("/${this.class.simpleName}-template.xml"))
    }
}
