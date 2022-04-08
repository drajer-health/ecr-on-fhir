import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { PharoutingComponent } from './pharouting/pharouting.component';
import { PharoutinglistComponent } from './pharoutinglist/pharoutinglist.component';


const routes: Routes = [
  { path: 'configurepha', component: PharoutingComponent },
  { path: 'phalist', component: PharoutinglistComponent }
];

@NgModule({
  imports: [RouterModule.forRoot(routes,{ useHash: true })],
  exports: [RouterModule]
})
export class AppRoutingModule { }
