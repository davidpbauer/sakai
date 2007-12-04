/**********************************************************************************
 * $URL:  $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2007 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.scorm.ui.player.components;

import java.util.List;

import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.html.resources.CompressedResourceReference;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.scorm.model.api.SessionBean;
import org.sakaiproject.scorm.service.api.ScoBean;
import org.sakaiproject.scorm.service.api.ScormApplicationService;
import org.sakaiproject.scorm.service.api.ScormSequencingService;
import org.sakaiproject.scorm.ui.player.behaviors.SjaxCall;

public class CommunicationPanel extends Panel implements IHeaderContributor {
	public static final ResourceReference API = new CompressedResourceReference(
			CommunicationPanel.class, "res/API.js");

	public static final ResourceReference API_WRAPPER = new CompressedResourceReference(
			CommunicationPanel.class, "res/APIWrapper.js");

	private static final long serialVersionUID = 1L;

	private LaunchPanel launchPanel;
	
	@SpringBean
	ScormApplicationService applicationService;
	@SpringBean
	ScormSequencingService sequencingService;
	
	private SessionBean sessionBean;
	
	public CommunicationPanel(String id, final SessionBean sessionBean, final LaunchPanel launchPanel) {
		super(id);
		this.launchPanel = launchPanel;
		this.sessionBean = sessionBean;
		
		
		add(new ScormSjaxCall("Commit", 1));

		add(new ScormSjaxCall("GetDiagnostic", 1));

		add(new ScormSjaxCall("GetErrorString", 1));
	
		add(new ScormSjaxCall("GetLastError", 0));
		
		add(new ScormSjaxCall("GetValue", 1));

		SjaxCall initCall = new ScormSjaxCall("Initialize", 1) {
			private static final long serialVersionUID = 1L;

			protected String callMethod(ScoBean scoBean, AjaxRequestTarget target, Object... args) {
				updatePageSco(scoBean.getScoId(), target);
				
				ActivityTree tree = launchPanel.getTreePanel().getActivityTree();
				if (tree != null && !tree.isEmpty()) {
					tree.selectNode();
					tree.updateTree(target);
				}
				
				launchPanel.synchronizeState(sessionBean, target);

				return super.callMethod(scoBean, target, args);
			}
		};
		
		
		initCall.prependJavascript("tb_remove();");
		
		add(initCall);

		add(new ScormSjaxCall("SetValue", 2));

		add(new ScormSjaxCall("Terminate", 1) {
			private static final long serialVersionUID = 1L;

			protected String callMethod(ScoBean scoBean, AjaxRequestTarget target, Object... args) {
				String result = super.callMethod(scoBean, target, args);
						
				ActivityTree tree = launchPanel.getTreePanel().getActivityTree();
				if (tree != null && !tree.isEmpty()) {
					tree.selectNode();
					tree.updateTree(target);
				}
				
				applicationService.discardScoBean(scoBean.getScoId(), sessionBean, this);
				
				updatePageSco("", target);
				
				return result;
			}
		});
	}
	
	
	public void updatePageSco(String scoId, AjaxRequestTarget target) {
		if (target != null)
			target.appendJavascript("sco = '" + scoId + "';");
	}

	protected String getFirstArg(List<String> argumentValues) {
		if (null == argumentValues || argumentValues.size() <= 0)
			return "";

		return argumentValues.get(0);
	}
	
	protected String getSecondArg(List<String> argumentValues) {
		if (null == argumentValues || argumentValues.size() <= 1)
			return "";
		
		return argumentValues.get(1);
	}
	
	
	
	public void renderHead(IHeaderResponse response) {
		StringBuffer js = new StringBuffer();
		
		js.append("function APIAdapter() { };")	
			.append("var API_1484_11 = APIAdapter;\n")
			.append("var api_result = new Array();\n")
			.append("var call_number = 0;\n")
			.append("var sco = undefined;\n");
		
		response.renderJavascript(js.toString(), "apiAdapterJs");
	}
	
	
	public class ScormSjaxCall extends SjaxCall {
		
		private static final long serialVersionUID = 1L;
		
		public ScormSjaxCall(String event, int numArgs) {
			super(event, numArgs);
		}
		
		@Override
		protected SessionBean getSessionBean() {
			return sessionBean;
		}
		
		@Override
		protected ScormApplicationService getApplicationService() {
			return applicationService;
		}

		@Override
		protected ScormSequencingService getSequencingService() {
			return sequencingService;
		}
		
		@Override
		protected String getChannelName() {
			return "1|s";
		}
	}
	
	
}
