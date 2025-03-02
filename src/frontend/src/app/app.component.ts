import { Component } from '@angular/core';

import {ModulesListComponent} from "./modules-list/modules-list.component";

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [ModulesListComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {

}
