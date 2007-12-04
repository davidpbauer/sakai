/*******************************************************************************
**
** Advanced Distributed Learning Co-Laboratory (ADL Co-Lab) Hub grants you 
** ("Licensee") a non-exclusive, royalty free, license to use, modify and 
** redistribute this software in source and binary code form, provided that 
** i) this copyright notice and license appear on all copies of the software; 
** and ii) Licensee does not utilize the software in a manner which is 
** disparaging to ADL Co-Lab Hub.
**
** This software is provided "AS IS," without a warranty of any kind.  ALL 
** EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING 
** ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE 
** OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED.  ADL Co-Lab Hub AND ITS LICENSORS 
** SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF 
** USING, MODIFYING OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES.  IN NO 
** EVENT WILL ADL Co-Lab Hub OR ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE, 
** PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, 
** INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE 
** THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE 
** SOFTWARE, EVEN IF ADL Co-Lab Hub HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH 
** DAMAGES.
**
*******************************************************************************/

package org.adl.datamodels;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.adl.datamodels.ieee.IValidatorFactory;
import org.sakaiproject.scorm.client.api.ScormClientFacade;


/**
 * <strong>Filename:</strong>SCODataManager.java<br><br>
 * 
 * <strong>Description:</strong><br>
 * This class is responsible for maintaining the SCORM Data Model for a single
 * instance of a Sharable Content Object (SCO). The SCO Data Manager is
 * responsible for any interactions the LMS, learner or SCO may have with the
 * Data Model.<br><br>
 * 
 * <strong>Design Issues:</strong><br> none.
 * <br><br>
 * 
 * <strong>Implementation Issues:</strong><br> None
 * <br><br>
 * 
 * <strong>Known Problems:</strong> None<br><br>
 * 
 * <strong>Side Effects:</strong> None<br><br>
 * 
 * <strong>References:</strong> SCORM 2004<br>
 *  
 * @author ADL Technical Team
 */
public class SCODataManager implements IDataManager {

	private IValidatorFactory validatorFactory;
	
	private long id;
	
	// JLR --  Added these so we can persist and find SCODataManagers
	public String courseId = null;
	
	public String userId = null;
	
	public String title = null;
	
	// JLR -- We may want to add an attemptNum integer to track which attempt this is -- this will probably
	// need to be managed by the LMS
	
	
    /**
     * Describes the set of run-time data models managed for the SCO.
     */
	public Map mDataModels = null;
	
   /**
    * Default constructor required for serialization support. Its only action
    * is to create a null Hashtable mDataModels.
    */
   public SCODataManager() {
	   mDataModels = new Hashtable();
   }
	
   public SCODataManager(String courseId, String userId, String title) 
   {
	   this.courseId = courseId;
	   this.userId = userId;
	   this.title = title;
	   this.mDataModels = new Hashtable();
   }
      
   //public AbstractDataManager getAbstractDataManager() {
//   return new AbstractDataManager(mCourseID, mSCOID, mUserID, mDataModels);
  // }

   /**
    * Adds the identified data model to the set of run-time data models managed
    * for this SCO.  First checks the current set of managed data models
    * to ensure that the data model to be added is not aready present in the 
    * Hashtable.
    * 
    * @param iModel  Describes the run-time data model to be added.
    */
   public void addDM(int iModel)
   {
      // Create the indicated data model
      DataModel dm = DMFactory.createDM(iModel, validatorFactory);

      if ( dm != null )
      {
         // Make sure this data model isn't already being managed
         if ( mDataModels == null )
         {
            mDataModels = new Hashtable();

            mDataModels.put(dm.getDMBindingString(), dm);
         }
         else
         {
            DataModel check = 
               (DataModel)mDataModels.get(dm.getDMBindingString());

            if ( check == null )
            {
               mDataModels.put(dm.getDMBindingString(), dm);
            }
         }
      }
   }

   /**
    * Processes an equals() request against the SCO's run-time data.
    * 
    * @param iRequest The request (<code>DMRequest</code>) being processed.
    * 
    * @return A data model error code indicating the result of this
    *         operation.
    */
   public int equals(DMRequest iRequest)
   {

      // Assume no processing errors
      int result = DMErrorCodes.NO_ERROR;

      // Make sure there is a request to process and some data model to
      // process it on
      if ( iRequest != null && mDataModels != null )
      {
         RequestToken tok = iRequest.getNextToken();

         // The first request token must be a data model token
         if ( tok.getType() == RequestToken.TOKEN_DATA_MODEL )
         {

            DataModel dm = (DataModel)mDataModels.get(tok.getValue());

            // Make sure the data model exists
            if ( dm != null )
            {
               // Process this request
               result = dm.equals(iRequest);
            }
            else
            {
               // Specified data model element does not exist
               result = DMErrorCodes.UNDEFINED_ELEMENT;
            }
         }
         else
         {
            // No data model specified
            result = DMErrorCodes.INVALID_REQUEST;
         }
      }
      else
      {
         // Nothing to process or nothing to process on
         result = DMErrorCodes.UNKNOWN_EXCEPTION;
      }

      return result;
   }

   /**
    * Retrieves a specific Data Model managed by this 
    * <code>SCODataManager</code>.
    * 
    * @param iDataModel  Describes the dot-notation binding string of the
    *                    desired data model.
    * 
    * @return The <code>DataModel</code> object associated with the
    *         requested data model.
    */
   public DataModel getDataModel(String iDataModel)
   {
      DataModel dm = null;

      if ( mDataModels != null )
      {
         dm = (DataModel)mDataModels.get(iDataModel);
      }

      return dm;
   }

   /**
    * Processes a GetValue() request against the SCO's run-time data.
    * 
    * @param iRequest The request (<code>DMRequest</code>) being processed.
    * 
    * @param oInfo    Provides the value returned by this request.
    * 
    * @return A data model error code indicating the result of this
    *         operation.
    */
   public int getValue(DMRequest iRequest, DMProcessingInfo oInfo)
   {
      // Assume no processing errors
      int result = DMErrorCodes.NO_ERROR;

      // Make sure there is a request to process and some data model to
      // process it on
      if ( iRequest != null && mDataModels != null )
      {
         RequestToken tok = iRequest.getNextToken();

         // The first request token must be a data model token
         if ( tok.getType() == RequestToken.TOKEN_DATA_MODEL )
         {
            DataModel dm = (DataModel)mDataModels.get(tok.getValue());

            // Make sure the data model exists
            if ( dm != null )
            {
               // Process this request
               result = dm.getValue(iRequest, oInfo);
            }
            else
            {
               // Specified data model element does not exist
               result = DMErrorCodes.UNDEFINED_ELEMENT;
            }
         }
         else
         {
            // No data model specified
            result = DMErrorCodes.INVALID_REQUEST;
         }
      }
      else
      {
         // Nothing to process or nothing to process on
         result = DMErrorCodes.UNKNOWN_EXCEPTION;
      }

      return result;
   }

   /** 
    * Initializes all data models being managed for this SCO.
    */
   public void initialize()
   {
      if ( mDataModels != null )
      {
    	 for (Iterator it = mDataModels.values().iterator();it.hasNext();) {
    		 DataModel dm = (DataModel)it.next();
    		 dm.initialize();
         }
      }
   }

   /**
    * Processes a SetValue() request against the SCO's run-time data.
    * 
    * @param iRequest The request (<code>DMRequest</code>) being processed.
    * 
    * @return A data model error code indicating the result of this
    *         operation.
    */
   public int setValue(DMRequest iRequest)
   {
      // Assume no processing errors
      int result = DMErrorCodes.NO_ERROR;

      // Make sure there is a request to process and some data model to
      // process it on
      if ( iRequest != null && mDataModels != null )
      {
         RequestToken tok = iRequest.getNextToken();

         // The first request token must be a data model token
         if ( tok.getType() == RequestToken.TOKEN_DATA_MODEL )
         {
            DataModel dm = (DataModel)mDataModels.get(tok.getValue());

            // Make sure the data model exists
            if ( dm != null )
            {
               // Process this request
               result = dm.setValue(iRequest);
            }
            else
            {
               // Specified data model element does not exist
               result = DMErrorCodes.UNDEFINED_ELEMENT;
            }
         }
         else
         {
            // No data model specified
            result = DMErrorCodes.INVALID_REQUEST;
         }
      }
      else
      {
         // Nothing to process or nothing to process on
         result = DMErrorCodes.UNKNOWN_EXCEPTION;
      }

      return result;
   }

   /** 
    * Terminates all data models being managed for this SCO.
    */
   public void terminate()
   {
      if ( mDataModels != null )
      {
    	  for (Iterator it = mDataModels.values().iterator();it.hasNext();) {
     		 DataModel dm = (DataModel)it.next();
     		 dm.terminate();
    	  }
      }
   }

   /**
    * Processes a validate() request against the SCO's run-time data.
    * 
    * @param iRequest The request (<code>DMRequest</code>) being processed.
    * 
    * @return A data model error code indicating the result of this
    *         operation.
    */
   public int validate(DMRequest iRequest)
   {
      // Assume no processing errors
      int result = DMErrorCodes.NO_ERROR;

      // Make sure there is a request to process and some data model to
      // process it on
      if ( iRequest != null && mDataModels != null )
      {
         RequestToken tok = iRequest.getNextToken();

         // The first request token must be a data model token
         if ( tok.getType() == RequestToken.TOKEN_DATA_MODEL )
         {

            DataModel dm = (DataModel)mDataModels.get(tok.getValue());

            // Make sure the data model exists
            if ( dm != null )
            {
               // Process this request
               result = dm.validate(iRequest);
            }
            else
            {
               // Specified data model element does not exist
               result = DMErrorCodes.UNDEFINED_ELEMENT;
            }
         }
         else
         {
            // No data model specified
            result = DMErrorCodes.INVALID_REQUEST;
         }
      }
      else
      {
         // Nothing to process or nothing to process on
         result = DMErrorCodes.UNKNOWN_EXCEPTION;
      }

      return result;
   }

	public IValidatorFactory getValidatorFactory() {
		return validatorFactory;
	}
	
	public void setValidatorFactory(IValidatorFactory validatorFactory) {
		this.validatorFactory = validatorFactory;
	}
	
	public String getCourseId() {
		return courseId;
	}


	public void setCourseId(String courseId) {
		this.courseId = courseId;
	}


	public String getUserId() {
		return userId;
	}


	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getTitle() {
		return title;
	}

}  // SCODataManager
