import { Component, OnInit } from '@angular/core';
import { FormGroup, FormControl, Validators } from '@angular/forms'
import { PharoutingService } from '../pharouting.service';
import { Pharouting } from '../pharouting';
import { Router } from '@angular/router';

interface dropDownValues {
	value: string;
	viewValue: string;
}

@Component({
	selector: 'app-pharouting',
	templateUrl: './pharouting.component.html',
	styleUrls: ['./pharouting.component.css']
})

export class PharoutingComponent implements OnInit {

	constructor(private pharoutingService: PharoutingService, private router: Router) { }

	recProtocols: dropDownValues[] = [
		{ value: 'Direct', viewValue: 'Direct' },
		{ value: 'FHIR', viewValue: 'FHIR' },
	];


	protocolTypes: dropDownValues[] = [
		{ value: 'CDA_R11', viewValue: 'CDA_R11' },
		{ value: 'CDA_R30', viewValue: 'CDA_R30' },
		{ value: 'FHIR', viewValue: 'FHIR' },
	];

	statesLists: dropDownValues[] = [{ value: 'AK', viewValue: 'Alaska' },
	{ value: 'AL', viewValue: 'Alabama' },
	{ value: 'AR', viewValue: 'Arkansas' },
	{ value: 'AS', viewValue: 'American Samoa' },
	{ value: 'AZ', viewValue: 'Arizona' },
	{ value: 'CA', viewValue: 'California' },
	{ value: 'CO', viewValue: 'Colorado' },
	{ value: 'CT', viewValue: 'Connecticut' },
	{ value: 'DC', viewValue: 'District of Columbia' },
	{ value: 'DE', viewValue: 'Delaware' },
	{ value: 'FL', viewValue: 'Florida' },
	{ value: 'GA', viewValue: 'Georgia' },
	{ value: 'GU', viewValue: 'Guam' },
	{ value: 'HI', viewValue: 'Hawaii' },
	{ value: 'IA', viewValue: 'Iowa' },
	{ value: 'ID', viewValue: 'Idaho' },
	{ value: 'IL', viewValue: 'Illinois' },
	{ value: 'IN', viewValue: 'Indiana' },
	{ value: 'KS', viewValue: 'Kansas' },
	{ value: 'KY', viewValue: 'Kentucky' },
	{ value: 'LA', viewValue: 'Louisiana' },
	{ value: 'MA', viewValue: 'Massachusetts' },
	{ value: 'MD', viewValue: 'Maryland' },
	{ value: 'ME', viewValue: 'Maine' },
	{ value: 'MI', viewValue: 'Michigan' },
	{ value: 'MN', viewValue: 'Minnesota' },
	{ value: 'MO', viewValue: 'Missouri' },
	{ value: 'MS', viewValue: 'Mississippi' },
	{ value: 'MT', viewValue: 'Montana' },
	{ value: 'NC', viewValue: 'North Carolina' },
	{ value: 'ND', viewValue: 'North Dakota' },
	{ value: 'NE', viewValue: 'Nebraska' },
	{ value: 'NH', viewValue: 'New Hampshire' },
	{ value: 'NJ', viewValue: 'New Jersey' },
	{ value: 'NM', viewValue: 'New Mexico' },
	{ value: 'NV', viewValue: 'Nevada' },
	{ value: 'NY', viewValue: 'New York' },
	{ value: 'OH', viewValue: 'Ohio' },
	{ value: 'OK', viewValue: 'Oklahoma' },
	{ value: 'OR', viewValue: 'Oregon' },
	{ value: 'PA', viewValue: 'Pennsylvania' },
	{ value: 'PR', viewValue: 'Puerto Rico' },
	{ value: 'RI', viewValue: 'Rhode Island' },
	{ value: 'SC', viewValue: 'South Carolina' },
	{ value: 'SD', viewValue: 'South Dakota' },
	{ value: 'TN', viewValue: 'Tennessee' },
	{ value: 'TX', viewValue: 'Texas' },
	{ value: 'UT', viewValue: 'Utah' },
	{ value: 'VA', viewValue: 'Virginia' },
	{ value: 'VI', viewValue: 'Virgin Islands' },
	{ value: 'VT', viewValue: 'Vermont' },
	{ value: 'WA', viewValue: 'Washington' },
	{ value: 'WI', viewValue: 'Wisconsin' },
	{ value: 'WV', viewValue: 'West Virginia' },
	{ value: 'WY', viewValue: 'Wyoming' }
	];

	pharouting: Pharouting = new Pharouting();
	submitted = false;

	ngOnInit(): void {
		this.submitted = false;
	}

	routingForm = new FormGroup({
		agencycode: new FormControl('', Validators.required),
		receiverprotocol: new FormControl('', Validators.required),
		protocoltype: new FormControl('', Validators.required),
		endpointurl: new FormControl('', Validators.required),
		retrycount: new FormControl('', [
			Validators.required,
			Validators.pattern("^[0-9]*$"),
			Validators.maxLength(2),
		])
	})

	onSubmit() {
		if (!this.routingForm.valid) {
			console.log('not valid form submitted');
			return;
		} else {
			console.log(this.routingForm.value);
			this.validateAllFormFields(this.routingForm); //{7}
			this.pharouting = new Pharouting();
			this.pharouting.phaAgencyCode = this!.AgencyCode!.value;
			this.pharouting.receiverProtocol = this!.ReceiverProtocol!.value;
			this.pharouting.protocolType = this!.ProtocolType!.value;
			this.pharouting.endpointUrl = this!.EndpointUrl!.value;
			this.pharouting.retryCount = this!.RetryCount!.value;
			this.submitted = true;
			this.save();
			this.router.navigate(['/phalist']).then(() => {
				window.location.reload();
			  });
		}
	}

	save() {
		this.pharoutingService.createPhaRouting(this.pharouting)
			.subscribe(data => console.log(data), error => console.log(error));
		this.pharouting = new Pharouting();
	}

	get AgencyCode() {
		return this.routingForm.get('agencycode');
	}

	get ReceiverProtocol() {
		return this.routingForm.get('receiverprotocol');
	}

	get ProtocolType() {
		return this.routingForm.get('protocoltype');
	}

	get EndpointUrl() {
		return this.routingForm.get('endpointurl');
	}

	get RetryCount() {
		return this.routingForm.get('retrycount');
	}


	validateAllFormFields(formGroup: FormGroup) {         //{1}
		Object.keys(formGroup.controls).forEach(field => {  //{2}
			const control = formGroup.get(field);             //{3}
			if (control instanceof FormControl) {             //{4}
				control.markAsTouched({ onlySelf: true });
			} else if (control instanceof FormGroup) {        //{5}
				this.validateAllFormFields(control);            //{6}
			}
		});
	}
}
