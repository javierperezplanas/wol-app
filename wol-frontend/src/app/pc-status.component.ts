import { Component, inject, OnInit, OnDestroy, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { WolService } from './wol.service';

@Component({
  selector: 'app-pc-status',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './pc-status.component.html',
  styleUrl: './pc-status.component.css'
})
export class PcStatusComponent implements OnInit, OnDestroy {
  private wolService = inject(WolService);
  private timer: any;

  isOnline = signal<boolean>(false);
  isLoading = signal<boolean>(true);
  latencyMs = signal<number>(-1);

  ngOnInit() {
    this.check();
    // Recomprueba cada 5 segundos
    this.timer = setInterval(() => this.check(), 5000);
  }

  ngOnDestroy() {
    if (this.timer) {
      clearInterval(this.timer);
    }
  }

  check() {
    this.wolService.checkStatus().subscribe({
      next: (res) => {
        this.isOnline.set(res.online);
        this.latencyMs.set(res.latencyMs || -1);
        this.isLoading.set(false);
      },
      error: () => {
        // Asumimos apagado o backend caído
        this.isOnline.set(false);
        this.latencyMs.set(-1);
        this.isLoading.set(false);
      }
    });
  }
}
