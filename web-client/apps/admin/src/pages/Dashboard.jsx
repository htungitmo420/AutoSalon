import { Box, Typography, Container, Grid, Paper, Card, CardContent } from '@mui/material';
import TrendingUpIcon from '@mui/icons-material/TrendingUp';
import DirectionsCarIcon from '@mui/icons-material/DirectionsCar';
import AttachMoneyIcon from '@mui/icons-material/AttachMoney';
import BuildIcon from '@mui/icons-material/Build';

export default function Dashboard() {
  const statCards = [
    { title: 'Total Orders', value: '156', trend: '+12%', color: 'primary', icon: <TrendingUpIcon sx={{ fontSize: 40 }} /> },
    { title: 'Revenue', value: '$8.4M', trend: '+8%', color: 'success', icon: <AttachMoneyIcon sx={{ fontSize: 40 }} /> },
    { title: 'Inventory Stock', value: '42', trend: '-2%', color: 'info', icon: <DirectionsCarIcon sx={{ fontSize: 40 }} /> },
    { title: 'Active Assemblies', value: '12', trend: '+5%', color: 'warning', icon: <BuildIcon sx={{ fontSize: 40 }} /> },
  ];

  return (
    <Box sx={{ px: { xs: 2, md: 4 }, py: 4, width: '100%' }}>
      <Typography variant="h4" fontWeight="bold" gutterBottom>
        Dashboard Overview
      </Typography>
      
      <Grid container spacing={3} sx={{ mt: 2 }}>
        {statCards.map((stat, idx) => (
          <Grid item xs={12} sm={6} md={3} key={idx}>
            <Card sx={{ height: '100%', borderRadius: 2 }}>
              <CardContent sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                <Box>
                  <Typography color="text.secondary" gutterBottom>{stat.title}</Typography>
                  <Typography variant="h4" fontWeight="bold">{stat.value}</Typography>
                  <Typography variant="body2" color={stat.trend.startsWith('+') ? 'success.main' : 'error.main'} sx={{ display: 'flex', alignItems: 'center', mt: 1 }}>
                    <TrendingUpIcon fontSize="small" sx={{ mr: 0.5 }} />
                    {stat.trend}
                  </Typography>
                </Box>
                <Box sx={{ bgcolor: `${stat.color}.light`, p: 1.5, borderRadius: 2, color: `${stat.color}.main` }}>
                  {stat.icon}
                </Box>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>
      
      <Box mt={6}>
        <Typography variant="h5" fontWeight="bold" mb={3}>Recent Activity</Typography>
        <Paper elevation={2} sx={{ borderRadius: 3, p: 3 }}>
          <Typography color="text.secondary">Activity stream will be hooked up to Kafka Read Model...</Typography>
        </Paper>
      </Box>
    </Box>
  );
}
