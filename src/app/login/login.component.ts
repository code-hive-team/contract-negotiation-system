import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {

  username = '';
  password = '';
  errorMessage = '';

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  login() {

    if (!this.username || !this.password) {
      this.errorMessage = 'Please enter username/email and password.';
      return;
    }

    const user = {
      username: this.username,
      password: this.password
    };

    this.authService.login(user).subscribe({

      next: (response: any) => {

        alert('Login Successful');

        // Optional: Store JWT token if backend returns one
        if (response.token) {
          localStorage.setItem('token', response.token);
        }

        this.router.navigate(['/dashboard']);
      },

      error: (err) => {

        if (err.status === 401) {
          this.errorMessage = 'Invalid username or password.';
        } else {
          this.errorMessage = 'Login failed.';
        }

      }

    });

  }

}