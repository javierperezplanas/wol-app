import { Component, OnInit } from '@angular/core';

interface ChartData {
  time: string;
  count: number;
  height: string;
}

@Component({
  selector: 'app-power-chart',
  templateUrl: './power-chart.component.html',
  styleUrl: './power-chart.component.css',
  standalone: true
})
export class PowerChartComponent implements OnInit {
  public chartData: ChartData[] = [];
  public totalPowerOns: number = 0;

  ngOnInit() {
    this.generateMockData();
  }

  private generateMockData() {
    // Simulamos datos de encendidos para el día de hoy
    const rawData = [
      { time: '08:00', count: 1 },
      { time: '10:00', count: 3 },
      { time: '14:00', count: 2 },
      { time: '18:00', count: 5 },
      { time: '21:00', count: 1 },
    ];
    
    this.totalPowerOns = rawData.reduce((acc, curr) => acc + curr.count, 0);
    const maxCount = Math.max(...rawData.map(d => d.count), 1);
    
    this.chartData = rawData.map(d => ({
      ...d,
      height: `${(d.count / maxCount) * 100}%`
    }));
  }
}
