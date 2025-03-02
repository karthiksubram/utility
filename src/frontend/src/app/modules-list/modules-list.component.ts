
import { Component, OnInit } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';

import {AppData} from "../models/appData";
import {ModulesService} from "../services/modules.service";
import { NgIf, NgFor } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatGridListModule } from '@angular/material/grid-list';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import {MatIconModule} from "@angular/material/icon";
import {MatListModule} from "@angular/material/list";

@Component({
  selector: 'app-modules-list',
  standalone: true,
  imports: [NgIf, NgFor, MatButtonModule, MatCardModule, MatGridListModule, MatSnackBarModule, MatIconModule,MatListModule],
  templateUrl: './modules-list.component.html',
  styleUrl: './modules-list.component.css'
})
export class ModulesListComponent implements OnInit {
  apps: AppData[] = [];

  constructor(private modulesService: ModulesService, private snackBar: MatSnackBar) {}

  ngOnInit(): void {
    this.fetchApps();
  }

  fetchApps(): void {
    this.modulesService.getApps().subscribe(
      (data) => (this.apps = data),
      () => this.showNotification('Error fetching apps')
    );
  }

  startApp(id: string): void {
    this.modulesService.startApp(id).subscribe(
      () => {
        this.updateAppStatus(id, 'running');
        this.showNotification('App started');
      },
      () => this.showNotification('Failed to start app')
    );
  }

  stopApp(id: string): void {
    this.modulesService.stopApp(id).subscribe(
      () => {
        this.updateAppStatus(id, 'stopped');
        this.showNotification('App stopped');
      },
      () => this.showNotification('Failed to stop app')
    );
  }

  deleteApp(id: string): void {
    this.modulesService.deleteApp(id).subscribe(
      () => {
        this.apps = this.apps.filter(app => app.id !== id);
        this.showNotification('App deleted');
      },
      () => this.showNotification('Failed to delete app')
    );
  }

  registerApp(): void {
    this.showNotification('Register new app clicked');
    // Implement registration logic
  }

  updateAppStatus(id: string, status: string): void {
    this.apps = this.apps.map(app => app.id === id ? { ...app, status } : app);
  }

  showNotification(message: string): void {
    this.snackBar.open(message, 'Close', { duration: 3000 });
  }
}
