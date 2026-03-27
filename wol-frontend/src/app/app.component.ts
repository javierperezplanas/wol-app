import { Component, signal } from '@angular/core';
import { WolButtonComponent } from './wol-button.component';
import { PcStatusComponent } from './pc-status.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [WolButtonComponent, PcStatusComponent],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class AppComponent {
  protected readonly title = signal('wol-frontend');
}
