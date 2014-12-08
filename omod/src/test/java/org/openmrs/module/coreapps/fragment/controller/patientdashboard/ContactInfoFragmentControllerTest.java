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
package org.openmrs.module.coreapps.fragment.controller.patientdashboard;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Patient;
import org.openmrs.module.emrapi.patient.PatientDomainWrapper;
import org.openmrs.ui.framework.fragment.FragmentConfiguration;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class ContactInfoFragmentControllerTest {

	ContactInfoFragmentController controller;

	FragmentConfiguration config;

	PatientDomainWrapper wrapper;

	@Before
	public void before() {
		controller = new ContactInfoFragmentController();
		config = new FragmentConfiguration();
		wrapper = mock(PatientDomainWrapper.class);
	}

	@Test
	public void shouldReturnPatientAndHideEditInfoContactButtonAttributes() {
		//given
		Patient patient = new Patient();
		config.addAttribute("patient", patient);

		//when
		controller.controller(config, wrapper);

		//then
		assertThat(config.get("patient"), is(instanceOf(PatientDomainWrapper.class)));
	}
}
