import React from 'react';
import { Outlet } from 'react-router-dom';

const Notes = () => {
  return (
    <div>
      <Outlet />
    </div>
  );
};

export default Notes;