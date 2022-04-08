import { NgModule, Inject, APP_INITIALIZER } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';  
import { HttpClientModule } from '@angular/common/http';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { MatSliderModule } from '@angular/material/slider';
import { MatNativeDateModule } from '@angular/material/core';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSortModule } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { MatRadioModule } from '@angular/material/radio';
import { PharoutingComponent } from './pharouting/pharouting.component';
import { PharoutinglistComponent } from './pharoutinglist/pharoutinglist.component';
import { AppConfigService } from './AppConfigService';

const appInitializerFn = (appConfig: AppConfigService) => {
    return () => {
        return appConfig.loadAppConfig();
    }
};

@NgModule({
  declarations: [
    AppComponent,
    PharoutingComponent,
    PharoutinglistComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    ReactiveFormsModule,
	CommonModule,
	HttpClientModule,
    BrowserAnimationsModule,
	MatSliderModule,
	MatNativeDateModule,
	MatDatepickerModule,
	MatButtonModule,
	MatCheckboxModule,
	MatFormFieldModule,
	MatInputModule,
	MatSortModule,
	MatTableModule,
	MatIconModule,
	MatSelectModule,
	MatRadioModule
  ],
  providers: [
	AppConfigService,
	{
		provide: APP_INITIALIZER,
		useFactory: appInitializerFn,
		multi: true,
		deps: [AppConfigService]
	}
],
  bootstrap: [AppComponent]
})
export class AppModule { }
