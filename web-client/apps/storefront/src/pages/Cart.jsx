import { useState } from 'react';
import { Container, Typography, Card, CardContent, Grid, Button, Box, TextField, Divider, Paper, Stepper, Step, StepLabel } from '@mui/material';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';

import { useQuery } from '@tanstack/react-query';
import api from '../services/api';

const fetchCart = async () => {
  try {
    const res = await api.get('/v1/cart');
    return res.data;
  } catch (error) {
    // Return mock cart item for local testing if backend is down
    return {
      items: [{
        id: 1,
        name: 'Sedan X',
        price: 45000,
        imageUrl: 'https://images.unsplash.com/photo-1549399542-7e3f8b79c341?auto=format&fit=crop&w=400&q=80'
      }]
    };
  }
};

export default function Cart() {
  const [activeStep, setActiveStep] = useState(0);
  const [cardNumber, setCardNumber] = useState('');
  const [expiry, setExpiry] = useState('');
  const [cvc, setCvc] = useState('');
  const [isProcessing, setIsProcessing] = useState(false);

  const { data: cartData, isLoading } = useQuery({
    queryKey: ['cart'],
    queryFn: fetchCart,
  });

  const cartItem = cartData?.items?.[0] || null;

  const handlePayment = (e) => {
    e.preventDefault();
    setIsProcessing(true);
    setTimeout(() => {
      setIsProcessing(false);
      setActiveStep(2);
    }, 1500);
  };

  const steps = ['Review Cart', 'Payment Details', 'Confirmation'];

  if (isLoading) return <Box textAlign="center" mt={10}><Typography>Loading Cart...</Typography></Box>;

  return (
    <Box sx={{ py: 6, px: { xs: 3, md: 8, lg: 12 } }}>
      <Typography variant="h3" fontWeight="bold" gutterBottom textAlign="center">
        Checkout
      </Typography>

      <Stepper activeStep={activeStep} sx={{ mb: 6, mt: 4, maxWidth: 800, mx: 'auto' }}>
        {steps.map((label) => (
          <Step key={label}>
            <StepLabel>{label}</StepLabel>
          </Step>
        ))}
      </Stepper>

      {activeStep === 2 ? (
        <Paper sx={{ p: 6, textAlign: 'center', borderRadius: 4, maxWidth: 600, mx: 'auto' }}>
          <CheckCircleIcon color="success" sx={{ fontSize: 80, mb: 2 }} />
          <Typography variant="h4" gutterBottom fontWeight="bold">Payment Successful!</Typography>
          <Typography variant="h6" color="text.secondary" mb={4}>
            Your order for the {cartItem?.name} has been confirmed.
          </Typography>
          <Button variant="contained" size="large" onClick={() => window.location.href='/'} sx={{ borderRadius: 2 }}>
            Return to Home
          </Button>
        </Paper>
      ) : (
        <Box sx={{ display: 'flex', flexDirection: { xs: 'column', md: 'row' }, gap: 4 }}>
          <Box sx={{ flex: { xs: '1', md: '0 0 60%' } }}>
            <Typography variant="h5" fontWeight="bold" mb={3}>Order Items</Typography>
            {cartItem ? (
              <Card sx={{ display: 'flex', flexDirection: { xs: 'column', sm: 'row' }, mb: 2, borderRadius: 3, overflow: 'hidden' }}>
                <Box component="img" sx={{ width: { xs: '100%', sm: 200 }, height: { xs: 200, sm: 'auto' }, objectFit: 'cover' }} src={cartItem.imageUrl} alt={cartItem.name} />
                <Box sx={{ display: 'flex', flexDirection: 'column', flexGrow: 1 }}>
                  <CardContent sx={{ flex: '1 0 auto', p: 3 }}>
                    <Typography component="div" variant="h5" fontWeight="bold">
                      {cartItem.name}
                    </Typography>
                    <Typography variant="subtitle1" color="text.secondary" component="div">
                      Base Model • Standard Package
                    </Typography>
                    <Typography variant="h5" color="primary.main" sx={{ mt: 2, fontWeight: 'bold' }}>
                      ${cartItem.price?.toLocaleString()}
                    </Typography>
                  </CardContent>
                </Box>
              </Card>
            ) : (
               <Typography color="text.secondary">Your cart is empty.</Typography>
            )}
          </Box>

          <Box sx={{ flex: { xs: '1', md: '0 0 35%' } }}>
            <Paper sx={{ p: 4, borderRadius: 3, position: 'sticky', top: 100 }}>
              <Typography variant="h5" fontWeight="bold" gutterBottom>Order Summary</Typography>
              <Box display="flex" justifyContent="space-between" mb={1} mt={3}>
                <Typography color="text.secondary">Vehicle Price</Typography>
                <Typography>${cartItem?.price?.toLocaleString() || 0}</Typography>
              </Box>
              <Box display="flex" justifyContent="space-between" mb={1}>
                <Typography color="text.secondary">Taxes & Fees</Typography>
                <Typography>$4,500</Typography>
              </Box>
              <Divider sx={{ my: 2 }} />
              <Box display="flex" justifyContent="space-between" mb={4}>
                <Typography variant="h5" fontWeight="bold">Total</Typography>
                <Typography variant="h5" fontWeight="bold" color="primary.main">${((cartItem?.price || 0) + 4500).toLocaleString()}</Typography>
              </Box>

              {activeStep === 0 && (
                <Button 
                  variant="contained" 
                  fullWidth 
                  size="large"
                  onClick={() => setActiveStep(1)}
                  sx={{ py: 1.5, borderRadius: 2 }}
                  disabled={!cartItem}
                >
                  Proceed to Payment
                </Button>
              )}

              {activeStep === 1 && (
                <Box component="form" onSubmit={handlePayment}>
                  <Typography variant="subtitle1" fontWeight="bold" mb={2}>Secure Card Payment</Typography>
                  <TextField fullWidth label="Card Number" variant="outlined" sx={{ mb: 2 }} value={cardNumber} onChange={e => setCardNumber(e.target.value)} required />
                  <Box display="flex" gap={2} mb={3}>
                    <TextField fullWidth label="MM/YY" variant="outlined" value={expiry} onChange={e => setExpiry(e.target.value)} required />
                    <TextField fullWidth label="CVC" variant="outlined" value={cvc} onChange={e => setCvc(e.target.value)} required />
                  </Box>
                  <Button type="submit" variant="contained" color="secondary" fullWidth size="large" sx={{ py: 1.5, borderRadius: 2 }} disabled={isProcessing}>
                    {isProcessing ? 'Processing...' : 'Pay Now'}
                  </Button>
                  <Button fullWidth onClick={() => setActiveStep(0)} sx={{ mt: 1 }}>
                    Back to Cart
                  </Button>
                </Box>
              )}
            </Paper>
          </Box>
        </Box>
      )}
    </Box>
  );
}
