import { createTheme } from '@mui/material/styles';

const getDesignTokens = (mode) => ({
  palette: {
    mode,
    primary: {
      main: '#D4AF37', // Gold/Premium feel
      dark: '#B08D28',
      light: '#E2C76A',
    },
    secondary: {
      main: mode === 'light' ? '#111111' : '#f5f5f5',
    },
    background: {
      default: 'transparent', // Let index.css fluid background show through
      paper: mode === 'light' ? 'rgba(255, 255, 255, 0.45)' : 'rgba(18, 18, 22, 0.65)',
    },
    text: {
      primary: mode === 'light' ? '#000000' : '#ffffff',
      secondary: mode === 'light' ? 'rgba(0, 0, 0, 0.6)' : 'rgba(255, 255, 255, 0.7)',
    },
    divider: mode === 'light' ? 'rgba(0, 0, 0, 0.08)' : 'rgba(255, 255, 255, 0.08)',
  },
  typography: {
    fontFamily: '"Inter", "Helvetica", "Arial", sans-serif',
    h1: { fontFamily: '"Space Grotesk", sans-serif', fontWeight: 900 },
    h2: { fontFamily: '"Space Grotesk", sans-serif', fontWeight: 900 },
    h3: { fontFamily: '"Space Grotesk", sans-serif', fontWeight: 700 },
    h4: { fontFamily: '"Space Grotesk", sans-serif', fontWeight: 700 },
    h5: { fontFamily: '"Space Grotesk", sans-serif', fontWeight: 700 },
    h6: { fontFamily: '"Space Grotesk", sans-serif', fontWeight: 700 },
    button: {
      textTransform: 'none',
      fontWeight: 600,
    },
  },
  shape: {
    borderRadius: 16,
  },
  components: {
    MuiCssBaseline: {
      styleOverrides: {
        body: {
          backgroundAttachment: 'fixed',
        },
      },
    },
    MuiButton: {
      styleOverrides: {
        root: {
          borderRadius: 24, // Capsule shapes for iOS 26 style
          padding: '10px 24px',
          boxShadow: 'none',
          transition: 'all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1)',
          '&:hover': {
            transform: 'translateY(-2px) scale(1.02)',
            boxShadow: mode === 'light' 
              ? '0 8px 20px rgba(212, 175, 55, 0.25)' 
              : '0 8px 20px rgba(212, 175, 55, 0.4)',
          },
          '&:active': {
            transform: 'translateY(0) scale(0.98)',
          },
        },
        containedPrimary: {
          background: 'linear-gradient(135deg, #E2C76A 0%, #D4AF37 100%)',
          color: '#000000',
          fontWeight: 700,
          '&:hover': {
            background: 'linear-gradient(135deg, #ebd68a 0%, #b08d28 100%)',
          },
        },
      },
    },
    MuiCard: {
      styleOverrides: {
        root: {
          backgroundImage: 'none',
          backgroundColor: mode === 'light' ? 'rgba(255, 255, 255, 0.45)' : 'rgba(18, 18, 22, 0.65)',
          backdropFilter: 'blur(30px) saturate(210%)',
          WebkitBackdropFilter: 'blur(30px) saturate(210%)',
          borderRadius: 24, // Very smooth rounded corners
          border: mode === 'light' 
            ? '1px solid rgba(255, 255, 255, 0.5)' 
            : '1px solid rgba(255, 255, 255, 0.1)',
          boxShadow: mode === 'light'
            ? '0 12px 40px rgba(31, 38, 135, 0.04), inset 0 1px 0 0 rgba(255, 255, 255, 0.5)'
            : '0 12px 40px rgba(0, 0, 0, 0.4), inset 0 1px 0 0 rgba(255, 255, 255, 0.06)',
          transition: 'transform 0.4s cubic-bezier(0.16, 1, 0.3, 1), box-shadow 0.4s ease',
        },
      },
    },
    MuiPaper: {
      styleOverrides: {
        root: {
          backgroundImage: 'none',
          backgroundColor: mode === 'light' ? 'rgba(255, 255, 255, 0.45)' : 'rgba(18, 18, 22, 0.65)',
          backdropFilter: 'blur(30px) saturate(210%)',
          WebkitBackdropFilter: 'blur(30px) saturate(210%)',
          borderRadius: 24,
          border: mode === 'light' 
            ? '1px solid rgba(255, 255, 255, 0.5)' 
            : '1px solid rgba(255, 255, 255, 0.1)',
          boxShadow: mode === 'light'
            ? '0 12px 40px rgba(31, 38, 135, 0.04), inset 0 1px 0 0 rgba(255, 255, 255, 0.5)'
            : '0 12px 40px rgba(0, 0, 0, 0.4), inset 0 1px 0 0 rgba(255, 255, 255, 0.06)',
        },
      },
    },
    MuiAppBar: {
      styleOverrides: {
        root: {
          backgroundColor: mode === 'light' ? 'rgba(255, 255, 255, 0.45)' : 'rgba(8, 8, 10, 0.65)',
          backdropFilter: 'blur(30px) saturate(210%)',
          WebkitBackdropFilter: 'blur(30px) saturate(210%)',
          borderBottom: mode === 'light' 
            ? '1px solid rgba(255, 255, 255, 0.4)' 
            : '1px solid rgba(255, 255, 255, 0.08)',
          backgroundImage: 'none',
          boxShadow: 'none',
          color: mode === 'light' ? '#000000' : '#ffffff',
        },
      },
    },
    MuiOutlinedInput: {
      styleOverrides: {
        root: {
          borderRadius: 16,
          backgroundColor: mode === 'light' ? 'rgba(255, 255, 255, 0.3)' : 'rgba(0, 0, 0, 0.2)',
          backdropFilter: 'blur(10px)',
          WebkitBackdropFilter: 'blur(10px)',
          transition: 'all 0.3s ease',
          '&:hover': {
            backgroundColor: mode === 'light' ? 'rgba(255, 255, 255, 0.5)' : 'rgba(0, 0, 0, 0.3)',
          },
          '&.Mui-focused': {
            backgroundColor: mode === 'light' ? 'rgba(255, 255, 255, 0.6)' : 'rgba(0, 0, 0, 0.4)',
          },
        },
      },
    },
  },
});

const getTheme = (mode) => createTheme(getDesignTokens(mode));
export default getTheme;
