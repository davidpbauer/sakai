package org.sakaiproject.scorm.ui.player.components;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.ComponentTag;
import org.sakaiproject.scorm.client.api.IRunState;
import org.sakaiproject.scorm.model.api.SessionBean;
import org.sakaiproject.scorm.ui.player.behaviors.ActivityAjaxEventBehavior;

public class CloseWindowBehavior extends ActivityAjaxEventBehavior {

	private static final long serialVersionUID = 1L;

	private static Log log = LogFactory.getLog(CloseWindowBehavior.class);
	
	private SessionBean sessionBean;
	
	public CloseWindowBehavior(SessionBean sessionBean) {
		super("closeWindow");
		this.sessionBean = sessionBean;
	}
	
	@Override
	protected void onEvent(AjaxRequestTarget target) {
		log.warn("closeWindowBehavior onEvent()");
		if (sessionBean != null && sessionBean.isStarted() && !sessionBean.isEnded()) {
			log.warn("----> Going to exit on next terminate request");
			sessionBean.setCloseOnNextTerminate(true);
		}
	}
	
	@Override
	protected void onComponentTag(final ComponentTag tag) {
		// Do nothing -- we don't want to add the javascript to the component.
	}

}
