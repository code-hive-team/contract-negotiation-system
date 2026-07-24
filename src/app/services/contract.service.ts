import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ContractService {

  private apiUrl = 'http://13.202.79.56:8080/api';
  constructor(private http: HttpClient) {}

  compareContracts(issuer: File, acquirer: File): Observable<any> {

    const formData = new FormData();

    formData.append('issuerFile', issuer);
    formData.append('acquirerFile', acquirer);

    const token = localStorage.getItem('token');

    const headers = new HttpHeaders({
      Authorization: `Bearer ${token}`
    });

    return this.http.post(
      `${this.apiUrl}/compare`,
      formData,
      { headers }
    );
  }
}