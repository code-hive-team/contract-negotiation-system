import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { ContractService } from '../services/contract.service';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent {

  issuerFile!: File;
  acquirerFile!: File;

  issuerFileName: string = '';
  acquirerFileName: string = '';

  issuerSelected: boolean = false;
  acquirerSelected: boolean = false;

  loading: boolean = false;

  constructor(
    private contractService: ContractService,
    private router: Router
  ) {}

  // Issuer File Selection
  onIssuerSelected(event: any): void {

    if (event.target.files.length > 0) {

      this.issuerFile = event.target.files[0];
      this.issuerFileName = this.issuerFile.name;
      this.issuerSelected = true;

    }

  }

  // Acquirer File Selection
  onAcquirerSelected(event: any): void {

    if (event.target.files.length > 0) {

      this.acquirerFile = event.target.files[0];
      this.acquirerFileName = this.acquirerFile.name;
      this.acquirerSelected = true;

    }

  }

  // Compare Contracts
  compareContracts(): void {

    if (!this.issuerSelected || !this.acquirerSelected) {
      alert('Please upload both contracts.');
      return;
    }

    this.loading = true;

    this.contractService.compareContracts(
      this.issuerFile,
      this.acquirerFile
    ).subscribe({

      next: (response: any) => {

        this.loading = false;

        // Save negotiation result
        localStorage.setItem(
          'negotiationResult',
          JSON.stringify(response)
        );

        // Navigate to result page
        this.router.navigate(['/negotiation-result']);

      },

      error: (error) => {

        this.loading = false;

        console.error('Comparison Error:', error);

        alert('Contract comparison failed. Please try again.');

      }

    });

  }

}