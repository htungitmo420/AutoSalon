import { ThemeProvider, CssBaseline } from '@mui/material'
import useThemeStore from './store/themeStore'
import getTheme from './theme'
import { useMemo, useEffect } from 'react'

function ThemeWrapper({ children }) {
  const mode = useThemeStore((state) => state.mode)
  const theme = useMemo(() => getTheme(mode), [mode])

  useEffect(() => {
    document.documentElement.setAttribute('data-theme', mode)
  }, [mode])

  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      {children}
    </ThemeProvider>
  )
}

export default ThemeWrapper
