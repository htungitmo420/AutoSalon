import { useState } from 'react';
import { Container, Typography, Paper, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Button, Box, IconButton, Chip } from '@mui/material';
import UploadIcon from '@mui/icons-material/Upload';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';

export default function Inventory() {
  const [inventory] = useState([
    { id: 1, model: 'Sedan X', stock: 12, status: 'In Stock', price: 45000 },
    { id: 2, model: 'SUV Y', stock: 3, status: 'Low Stock', price: 65000 },
    { id: 3, model: 'Sports Z', stock: 0, status: 'Out of Stock', price: 85000 },
  ]);

  return (
    <Box sx={{ px: { xs: 2, md: 4 }, py: 4, width: '100%' }}>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={4}>
        <Typography variant="h4" fontWeight="bold">
          Inventory Management
        </Typography>
        <Button variant="contained" color="primary" startIcon={<UploadIcon />}>
          Add Vehicle
        </Button>
      </Box>

      <TableContainer component={Paper} sx={{ borderRadius: 2, overflow: 'hidden' }}>
        <Table>
          <TableHead sx={{ bgcolor: 'background.default' }}>
            <TableRow>
              <TableCell>ID</TableCell>
              <TableCell>Model</TableCell>
              <TableCell>Status</TableCell>
              <TableCell>Price</TableCell>
              <TableCell align="right">Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {inventory.map((row) => (
              <TableRow key={row.id}>
                <TableCell>{row.id}</TableCell>
                <TableCell sx={{ fontWeight: 'bold' }}>{row.model}</TableCell>
                <TableCell>{row.status}</TableCell>
                <TableCell>${row.price.toLocaleString()}</TableCell>
                <TableCell align="right">
                  <IconButton color="primary" size="small"><EditIcon /></IconButton>
                  <IconButton color="error" size="small"><DeleteIcon /></IconButton>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>
    </Box>
  );
}
