import { createTheme } from '@mui/material/styles';

const getDesignTokens = (mode) => ({
  palette: {
    mode,
    primary: {
      main: '#1976d2',
    },
    secondary: {
      main: '#dc004e',
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
    fontFamily: '"Inter", "Roboto", "Helvetica", "Arial", sans-serif',
    h5: {
      fontWeight: 600,
    },
    h6: {
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
          borderRadius: 24, // iOS capsule buttons
          textTransform: 'none',
          fontWeight: 600,
          transition: 'all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1)',
          '&:hover': {
            transform: 'translateY(-2px) scale(1.02)',
          },
          '&:active': {
            transform: 'translateY(0) scale(0.98)',
          },
        },
      },
    },
    MuiDrawer: {
      styleOverrides: {
        paper: {
          backgroundColor: mode === 'light' ? 'rgba(255, 255, 255, 0.45)' : 'rgba(15, 23, 42, 0.65)',
          backdropFilter: 'blur(30px) saturate(210%)',
          WebkitBackdropFilter: 'blur(30px) saturate(210%)',
          color: mode === 'light' ? '#000000' : '#ffffff',
          borderRight: mode === 'light' 
            ? '1px solid rgba(255, 255, 255, 0.4)' 
            : '1px solid rgba(255, 255, 255, 0.08)',
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
          borderRadius: 24,
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
  },
});

const getTheme = (mode) => createTheme(getDesignTokens(mode));
export default getTheme;
