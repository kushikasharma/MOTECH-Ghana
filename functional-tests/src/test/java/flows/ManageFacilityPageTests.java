package flows;

import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.HomePageLinksEnum;
import pages.ManageFacilityPage;
import pages.MoTeCHDashBoardPage;
import pages.OpenMRSLoginPage;

/**
 * Created by IntelliJ IDEA.
 * User: rupeshd
 * Date: 2/15/11
 * Time: 12:41 PM
 * To change this template use File | Settings | File Templates.
 */
public class ManageFacilityPageTests {

    @Test
    public void AddCommunityTest(){
        OpenMRSLoginPage loginPage = new OpenMRSLoginPage();
        loginPage.getOpenMRSDashBoard();
        MoTeCHDashBoardPage moTeCHDashBoardPage = new MoTeCHDashBoardPage();
        moTeCHDashBoardPage.navigateToPage(HomePageLinksEnum.MANAGE_FACILITIES);
        ManageFacilityPage manageFacilityPage = new ManageFacilityPage();
        String facilityName = manageFacilityPage.AddNewFacility();
        Assert.assertNotNull(facilityName,"Facility Name is null.Facility is not created");
        Assert.assertTrue(manageFacilityPage.FindFacilityByName(facilityName), "Facility name not found");
   }
}
