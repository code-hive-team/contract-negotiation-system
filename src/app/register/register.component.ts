import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent {

  username = '';
  email = '';
  password = '';
  confirmPassword = '';
  errorMessage = '';

  constructor(
    private router: Router,
    private authService: AuthService
  ) {}

  register() {

    if (!this.username || !this.email || !this.password || !this.confirmPassword) {
      this.errorMessage = 'Please fill all fields.';
      return;
    }

    if (this.password !== this.confirmPassword) {
      this.errorMessage = 'Passwords do not match.';
      return;
    }

    const user = {
      username: this.username,
      email: this.email,
      password: this.password,
      role: 'ROLE_USER'
    };

    this.authService.register(user).subscribe({

      next: (response) => {

        alert('Registration Successful');

        this.router.navigate(['/dashboard']);

      },

      error: (err) => {

        if (err.status === 409) {
          this.errorMessage = 'Email already exists.';
        } else if (err.status === 400) {
          this.errorMessage = 'Invalid details.';
        } else {
          this.errorMessage = 'Registration failed.';
        }

      }

    });

  }

}