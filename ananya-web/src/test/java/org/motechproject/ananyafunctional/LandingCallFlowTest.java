package org.motechproject.ananyafunctional;

import org.junit.Before;
import org.junit.Test;
import org.motechproject.ananyafunctional.framework.CallFlow;
import org.motechproject.ananyafunctional.framework.MyWebClient;
import org.motechproject.bbcwt.repository.SpringIntegrationTest;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPathConstants;

import static junit.framework.Assert.assertEquals;

public class LandingCallFlowTest extends SpringIntegrationTest {

    private MyWebClient myWebClient;

    @Before
    public void setUp() {
        myWebClient = new MyWebClient();
    }

    @Test
    public void shouldGetLandingPageWithLinksToJobAidIfEntryIsThroughJobAidNumber() throws Exception {
        CallFlow callFlow = myWebClient.getCallFlow("http://localhost:9979/ananya/vxml/jobaid/landing/");
        NodeList links = (NodeList) callFlow.read("/vxml/form/block/if/goto", XPathConstants.NODESET);
        assertEquals("/vxml/jobaid/register", links.item(0).getAttributes().item(0).getTextContent());
        assertEquals("/vxml/jobaid.vxml", links.item(1).getAttributes().item(0).getTextContent());
    }

    @Test
    public void shouldGetLandingPageWithLinksToCourseIfEntryIsThroughCourseNumber() throws Exception {
        CallFlow callFlow = myWebClient.getCallFlow("http://localhost:9979/ananya/vxml/certificationcourse/landing/");
        NodeList links = (NodeList) callFlow.read("/vxml/form/block/if/goto", XPathConstants.NODESET);
        assertEquals("/vxml/certificationcourse/register", links.item(0).getAttributes().item(0).getTextContent());
        assertEquals("/vxml/certificationCourse.vxml", links.item(1).getAttributes().item(0).getTextContent());
    }
}
