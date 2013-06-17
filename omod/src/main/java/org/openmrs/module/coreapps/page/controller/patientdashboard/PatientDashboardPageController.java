/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.coreapps.page.controller.patientdashboard;

import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.api.APIException;
import org.openmrs.api.OrderService;
import org.openmrs.api.context.Context;
import org.openmrs.module.appframework.domain.Extension;
import org.openmrs.module.appframework.service.AppFrameworkService;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.emrapi.adt.AdtService;
import org.openmrs.module.emrapi.patient.PatientDomainWrapper;
import org.openmrs.module.emrapi.visit.VisitDomainWrapper;
import org.openmrs.ui.framework.annotation.InjectBeans;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;
import org.openmrs.ui.framework.page.Redirect;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PatientDashboardPageController {
	
	private static final String ENCOUNTER_TEMPLATE_EXTENSION = "org.openmrs.referenceapplication.encounterTemplate";
	
	public Object controller(@RequestParam("patientId") Patient patient,
	                       @RequestParam(value = "tab", defaultValue = "visits") String selectedTab, PageModel model,
	                       @InjectBeans PatientDomainWrapper patientDomainWrapper,
	                       @SpringBean("orderService") OrderService orderService,
	                       @SpringBean("adtService") AdtService adtService,
	                       @SpringBean("appFrameworkService") AppFrameworkService appFrameworkService,
	                       UiSessionContext sessionContext) {
		
        if (patient.isVoided() || patient.isPersonVoided()) {
            return new Redirect("coreapps", "patientdashboard/deletedPatient", "patientId=" + patient.getId());
        }

		patientDomainWrapper.setPatient(patient);
		model.addAttribute("patient", patientDomainWrapper);
		model.addAttribute("orders", orderService.getOrdersByPatient(patient));
		model.addAttribute("addressHierarchyLevels", getAddressHierarchyLevels());
		model.addAttribute("selectedTab", selectedTab);

        Location visitLocation = null;
        try {
            visitLocation = adtService.getLocationThatSupportsVisits(sessionContext.getSessionLocation());
        } catch (IllegalArgumentException ex) {
            // location does not support visits
        }
        if (visitLocation != null) {
            VisitDomainWrapper activeVisit = adtService.getActiveVisit(patient, visitLocation);
            model.addAttribute("activeVisit", activeVisit);
        } else {
            model.addAttribute("activeVisit", null);
        }

		List<Extension> encounterTemplateExtensions = appFrameworkService
		        .getExtensionsForCurrentUser(ENCOUNTER_TEMPLATE_EXTENSION);
		model.addAttribute("encounterTemplateExtensions", encounterTemplateExtensions);
		
		List<Extension> overallActions = appFrameworkService.getExtensionsForCurrentUser("patientDashboard.overallActions");
		Collections.sort(overallActions);
		model.addAttribute("overallActions", overallActions);
		
		List<Extension> visitActions = appFrameworkService.getExtensionsForCurrentUser("patientDashboard.visitActions");
		Collections.sort(visitActions);
		model.addAttribute("visitActions", visitActions);
		model.addAttribute("patientTabs", appFrameworkService.getExtensionsForCurrentUser("patientDashboard.tabs"));
        return null;
	}
	
	private static List<String> getAddressHierarchyLevels() {
		List<String> l = new ArrayList<String>();
		
		try {
			Class<?> svcClass = Context.loadClass("org.openmrs.module.addresshierarchy.service.AddressHierarchyService");
			Object svc = Context.getService(svcClass);
			List<Object> levels = (List<Object>) svcClass.getMethod("getOrderedAddressHierarchyLevels", Boolean.class,
			    Boolean.class).invoke(svc, true, true);
			Class<?> levelClass = Context.loadClass("org.openmrs.module.addresshierarchy.AddressHierarchyLevel");
			Class<?> fieldClass = Context.loadClass("org.openmrs.module.addresshierarchy.AddressField");
			for (Object o : levels) {
				Object addressField = levelClass.getMethod("getAddressField").invoke(o);
				String fieldName = (String) fieldClass.getMethod("getName").invoke(addressField);
				l.add(fieldName);
			}
			if (l.size() > 1) {
				Collections.reverse(l);
			}
		}
		catch (ClassNotFoundException cnfe) {
			//ignore, the module isn't installed
		}
		catch (Exception e) {
			throw new APIException("Error obtaining address hierarchy levels", e);
		}
		
		return l;
	}
}