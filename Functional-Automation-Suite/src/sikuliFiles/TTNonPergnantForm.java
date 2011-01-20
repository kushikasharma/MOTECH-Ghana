package sikuliFiles;
import java.text.ParseException;
import org.sikuli.script.FindFailed;

public class TTNonPergnantForm extends SikuliBaseClass{

    public void Fill_TT_Non_Pregnant_Form(String staffId, String facilityId,String date,String motechId, TTValues str) throws ParseException
	{ 
      try {
	     // selecting the TT_Non_pregnant form
    	  selectForm(FormName.TT_NON_PREGNANT);
          
         //Entering values in TT_Non_Pregnant form 
         //1. filling the staff id
          inputTextbox(staffId);
         
          //2. Filling the facility_id 
          inputTextbox(facilityId);
          
         //3. Filling the date of visit as current date 
          selectDate(stringToDateConvertor(date));
          
         //4.  Filling the motech id 
          inputTextbox(motechId);
          
         // 5. Filling the TT value as TT1
          
          selectTTValues(str);
                   
          //6. Saving the form
          saveMform();
          
          //7. Moving to Main Menu and uploading the form
          traverseToMainMenuAndUploadForm();
       
	} catch (FindFailed e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	}
}
	

