import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { WolService } from './wol.service';

type ButtonState = 'idle' | 'loading' | 'success' | 'error';

@Component({
  selector: 'app-wol-button',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './wol-button.component.html',
  styleUrl: './wol-button.component.css'
})
export class WolButtonComponent {
  private wolService = inject(WolService);
  
  state = signal<ButtonState>('idle');
  errorMessage = signal('');

  wakePc() {
    if (this.state() === 'loading') return;
    
    this.state.set('loading');
    this.errorMessage.set('');
    
    // Se envía la MAC real del PC
    this.wolService.wakeUp('38:05:25:33:78:92').subscribe({
      next: () => {
        this.state.set('success');
        setTimeout(() => this.state.set('idle'), 3000);
      },
      error: (err) => {
        this.state.set('error');
        this.errorMessage.set(err.message || 'Error al conectar con Quarkus');
        setTimeout(() => this.state.set('idle'), 4000);
      }
    });
  }
}
