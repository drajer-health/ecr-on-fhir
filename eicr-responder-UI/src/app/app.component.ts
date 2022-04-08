import { Component } from '@angular/core';
import { FormGroup, FormControl, Validators } from '@angular/forms'

import {
  trigger,
  state,
  style,
  animate,
  transition
} from '@angular/animations';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'eicr-responder-UI';
  submitted = false;
}
