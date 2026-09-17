document.addEventListener('DOMContentLoaded', () => {
  const toggleBtn = document.getElementById('toggleMode');
  const toggleText = document.getElementById('toggleText');
  const formTitle = document.getElementById('formTitle');
  const confirmPasswordWrapper = document.getElementById('confirmPasswordWrapper');
  const confirmPasswordInput = confirmPasswordWrapper.querySelector('input');
  const forgotPassword = document.getElementById('forgotPassword');
  const submitText = document.getElementById('submitText');
  const authForm = document.getElementById('authForm');

  let isLoginMode = true;

  toggleBtn.addEventListener('click', () => {
    isLoginMode = !isLoginMode;
    
    // Add a small animation effect on the form
    authForm.style.opacity = '0';
    
    setTimeout(() => {
      if (isLoginMode) {
        // Switch to Login Mode
        formTitle.textContent = 'LOGIN';
        toggleText.innerHTML = 'NÃO TEM UMA<br>CONTA?<br>CRIAR CONTA';
        confirmPasswordWrapper.style.display = 'none';
        confirmPasswordInput.removeAttribute('required');
        forgotPassword.style.display = 'block';
        submitText.textContent = 'ENTRAR';
      } else {
        // Switch to Register Mode
        formTitle.textContent = 'CADASTRAR';
        toggleText.innerHTML = 'JÁ TENHO UMA<br>CONTA<br>ENTRAR';
        confirmPasswordWrapper.style.display = 'block';
        confirmPasswordInput.setAttribute('required', 'true');
        forgotPassword.style.display = 'none';
        submitText.textContent = 'CRIAR';
      }
      
      authForm.style.opacity = '1';
    }, 200);
  });

  authForm.addEventListener('submit', (e) => {
    e.preventDefault();
    
    const emailInput = authForm.querySelector('input[type="email"]');
    const passwordInput = authForm.querySelector('input[type="password"]');
    
    if (!emailInput.value || !passwordInput.value) {
      return;
    }

    if (isLoginMode) {
      // Save email and navigate to home page
      localStorage.setItem('userEmail', emailInput.value);
      if (!localStorage.getItem('userName')) {
        localStorage.setItem('userName', 'Aventureiro');
      }
      sessionStorage.removeItem('newUser');
      sessionStorage.setItem('loggedIn', 'true');
      window.location.href = 'home.html';
    } else {
      // Register mode - show success then redirect
      const btn = document.getElementById('submitText');
      btn.textContent = 'CRIANDO...';
      setTimeout(() => {
        localStorage.setItem('userEmail', emailInput.value);
        if (!localStorage.getItem('userName')) {
          localStorage.setItem('userName', 'Aventureiro');
        }
        sessionStorage.setItem('newUser', 'true');
        sessionStorage.setItem('loggedIn', 'true');
        window.location.href = 'home.html';
      }, 800);
    }
  });
});
