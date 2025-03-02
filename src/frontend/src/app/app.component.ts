import { Component } from '@angular/core';
import { NgIf } from '@angular/common';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatIconModule } from '@angular/material/icon';

import {ModulesListComponent} from "./modules-list/modules-list.component";

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [ModulesListComponent,NgIf, MatToolbarModule, MatIconModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {

}
