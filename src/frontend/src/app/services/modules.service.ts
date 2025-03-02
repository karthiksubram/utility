import { Injectable } from '@angular/core';
import {AppData} from '../models/appData';
import { Observable, of } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ModulesService {

  constructor() { }

  private mockApps: AppData[] = [
    { id: '1', name: 'App One', status: 'stopped' },
    { id: '2', name: 'App Two', status: 'running' },
    { id: '3', name: 'App Three', status: 'stopped' }
  ];

  getApps(): Observable<AppData[]> {
    return of([...this.mockApps]);
  }

  startApp(id: string): Observable<void> {
    return of(void (this.mockApps.find(app => app.id === id)!.status = 'running'));
  }

  stopApp(id: string): Observable<void> {
    return of(void (this.mockApps.find(app => app.id === id)!.status = 'stopped'));
  }

  deleteApp(id: string): Observable<void> {
    this.mockApps = this.mockApps.filter(app => app.id !== id);
    return of();
  }
}
