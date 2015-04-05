package pl.wybcz.view
import javaposse.jobdsl.dsl.DslFactory
import javaposse.jobdsl.dsl.JobParent
import javaposse.jobdsl.dsl.View

class ViewBuilder {

    private final DslFactory dslFactory

    ViewBuilder(DslFactory dslFactory) {
        this.dslFactory = dslFactory
    }

    View appendDashboard() {
        JobParent jobParent = dslFactory as JobParent
        View view = new Dashboard('Overview', jobParent.jm)
        jobParent.referencedViews << view
        return view
    }
}
