import { useState } from 'react';
import axios from 'axios';
import { 
  Container, Box, Typography, TextField, Button, Paper, 
  MenuItem, Grid, Alert, AlertTitle, AppBar, Toolbar, 
  IconButton, CssBaseline, ThemeProvider, createTheme 
} from '@mui/material';
import { 
  Security as SecurityIcon, 
  AccountBalance as BankIcon, 
  Logout as LogoutIcon 
} from '@mui/icons-material';

// Kurumsal Koyu Tema Oluşturma ve Tarayıcı Autofill Düzeltmesi
const darkTheme = createTheme({
  palette: {
    mode: 'dark',
    primary: { main: '#1976d2' },
    secondary: { main: '#dc004e' },
    background: { default: '#0a1929', paper: '#1e293b' },
  },
  typography: { fontFamily: '"Roboto", "Helvetica", "Arial", sans-serif' },
  components: {
    MuiOutlinedInput: {
      styleOverrides: {
        input: {
          // Tarayıcının otomatik doldurma mavisini ezip kendi koyu temamıza uyarlıyoruz
          '&:-webkit-autofill': {
            WebkitBoxShadow: '0 0 0 100px #1e293b inset !important',
            WebkitTextFillColor: '#ffffff !important',
          },
        },
      },
    },
  },
});

function App() {
  const [token, setToken] = useState(localStorage.getItem('token') || '');
  const [loginData, setLoginData] = useState({ username: '', password: '' });
  const [loginError, setLoginError] = useState('');
  
  const [formData, setFormData] = useState({ identityNumber: '', requestedAmount: '', termMonths: '' });
  const [result, setResult] = useState(null);

  const handleLoginChange = (e) => setLoginData({ ...loginData, [e.target.name]: e.target.value });

  const handleLogin = async (e) => {
    e.preventDefault();
    try {
      const response = await axios.post('http://localhost:8080/api/auth/login', loginData);
      const receivedToken = response.data.token;
      setToken(receivedToken);
      localStorage.setItem('token', receivedToken);
      setLoginError('');
    } catch (error) {
      console.error("Giriş işlemi başarısız oldu:", error);
      setLoginError('Kimlik doğrulama başarısız. Lütfen yetkilerinizi kontrol edin.');
    }
  };

  const handleLogout = () => {
    setToken('');
    localStorage.removeItem('token');
    setResult(null);
  };

  const handleFormChange = (e) => setFormData({ ...formData, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const response = await axios.post('http://localhost:8080/api/loans/apply', formData, {
        headers: { Authorization: `Bearer ${token}` }
      });
      setResult(response.data);
    } catch (error) {
      if (error.response && error.response.status === 403) {
        setLoginError("Oturum süresi doldu.");
        handleLogout();
      } else {
        alert("Sistem Hatası: İşlem loglarını kontrol ediniz.");
      }
    }
  };

  // --- LOGIN EKRANI ---
  if (!token) {
    return (
      <ThemeProvider theme={darkTheme}>
        <CssBaseline />
        <Container component="main" maxWidth="xs" sx={{ height: '100vh', display: 'flex', alignItems: 'center' }}>
          <Paper elevation={6} sx={{ p: 4, display: 'flex', flexDirection: 'column', alignItems: 'center', width: '100%' }}>
            <SecurityIcon color="primary" sx={{ fontSize: 40, mb: 1 }} />
            <Typography component="h1" variant="h5" sx={{ mb: 3, fontWeight: 'bold' }}>
              CrediGuard Portal
            </Typography>
            
            {loginError && <Alert severity="error" sx={{ width: '100%', mb: 2 }}>{loginError}</Alert>}
            
            <Box component="form" onSubmit={handleLogin} sx={{ width: '100%' }}>
              <TextField margin="normal" required fullWidth label="Personel Sicil No / Kullanıcı Adı" name="username" autoFocus onChange={handleLoginChange} variant="outlined" />
              <TextField margin="normal" required fullWidth name="password" label="Güvenlik Şifresi" type="password" onChange={handleLoginChange} variant="outlined" />
              <Button type="submit" fullWidth variant="contained" sx={{ mt: 3, mb: 2, py: 1.5, fontWeight: 'bold' }}>
                Sisteme Giriş Yap
              </Button>
            </Box>
          </Paper>
        </Container>
      </ThemeProvider>
    );
  }

  // --- KREDİ BAŞVURU EKRANI (DASHBOARD) ---
  return (
    <ThemeProvider theme={darkTheme}>
      <CssBaseline />
      <AppBar position="static" color="transparent" elevation={0} sx={{ borderBottom: 1, borderColor: 'divider' }}>
        <Toolbar>
          <BankIcon sx={{ mr: 2, color: 'primary.main' }} />
          <Typography variant="h6" component="div" sx={{ flexGrow: 1, fontWeight: 'bold' }}>
            CrediGuard - Karar Destek Sistemi
          </Typography>
          <IconButton color="error" onClick={handleLogout} title="Güvenli Çıkış">
            <LogoutIcon />
          </IconButton>
        </Toolbar>
      </AppBar>

      <Container maxWidth="md" sx={{ mt: 4 }}>
        <Grid container spacing={4}>
          <Grid item xs={12} md={7}>
            <Paper elevation={3} sx={{ p: 4 }}>
              <Typography variant="h6" gutterBottom sx={{ fontWeight: 'bold', mb: 3 }}>
                Yeni Kredi Tahsis Formu
              </Typography>
              <Box component="form" onSubmit={handleSubmit}>
                <TextField fullWidth margin="normal" required label="Müşteri T.C. Kimlik No" name="identityNumber" inputProps={{ maxLength: 11 }} onChange={handleFormChange} />
                <TextField fullWidth margin="normal" required type="number" label="Talep Edilen Kredi Tutarı (₺)" name="requestedAmount" onChange={handleFormChange} />
                <TextField fullWidth margin="normal" required select label="Vade Seçeneği" name="termMonths" value={formData.termMonths} onChange={handleFormChange}>
                  <MenuItem value="12">12 Ay (Kısa Vade)</MenuItem>
                  <MenuItem value="24">24 Ay (Orta Vade)</MenuItem>
                  <MenuItem value="36">36 Ay (Uzun Vade)</MenuItem>
                </TextField>
                <Button type="submit" fullWidth variant="contained" size="large" sx={{ mt: 4, py: 1.5, fontWeight: 'bold' }}>
                  Risk Analizini Başlat
                </Button>
              </Box>
            </Paper>
          </Grid>

          <Grid item xs={12} md={5}>
            {result ? (
              <Paper elevation={3} sx={{ p: 3, borderTop: 6, borderColor: result.status === 'APPROVED' ? 'success.main' : result.status === 'REJECTED' ? 'error.main' : 'warning.main' }}>
                <Typography variant="h6" gutterBottom>Analiz Sonucu</Typography>
                <Alert severity={result.status === 'APPROVED' ? 'success' : result.status === 'REJECTED' ? 'error' : 'warning'} sx={{ mb: 2 }}>
                  <AlertTitle sx={{ fontWeight: 'bold' }}>Durum: {result.status}</AlertTitle>
                  Sistem Kararı
                </Alert>
                {result.rejectionReason && (
                  <Typography variant="body2" color="error" sx={{ mt: 2, p: 2, bgcolor: 'background.default', borderRadius: 1 }}>
                    <strong>Ret Gerekçesi:</strong> {result.rejectionReason}
                  </Typography>
                )}
                <Typography variant="caption" display="block" sx={{ mt: 3, color: 'text.secondary' }}>
                  İşlem Referans No: {result.id} | Tarih: {new Date(result.applicationDate).toLocaleString('tr-TR')}
                </Typography>
              </Paper>
            ) : (
               <Paper elevation={0} sx={{ p: 3, bgcolor: 'background.default', border: '1px dashed grey', display: 'flex', height: '100%', alignItems: 'center', justifyContent: 'center' }}>
                  <Typography variant="body1" color="text.secondary" align="center">
                    Analiz sonucu burada görüntülenecektir. Lütfen formu doldurup işlemi başlatın.
                  </Typography>
               </Paper>
            )}
          </Grid>
        </Grid>
      </Container>
    </ThemeProvider>
  );
}

export default App;