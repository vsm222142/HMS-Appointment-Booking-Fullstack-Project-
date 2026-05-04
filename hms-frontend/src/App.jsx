import React, { lazy, Suspense, useContext } from 'react'
import Navbar from './components/Navbar'
import { Routes, Route } from 'react-router-dom'
import { ToastContainer } from 'react-toastify'   
import 'react-toastify/dist/ReactToastify.css'
import { AppContext } from './context/AppContext'
import Footer from './components/Footer'

// Lazy Loading Components
const Home = lazy(() => import('./pages/Home'))
const Doctors = lazy(() => import('./pages/Doctors'))
const Login = lazy(() => import('./pages/Login'))
const About = lazy(() => import('./pages/About'))
const Contact = lazy(() => import('./pages/Contact'))
const Appointment = lazy(() => import('./pages/Appointment'))
const MyAppointments = lazy(() => import('./pages/MyAppointments'))
const MyProfile = lazy(() => import('./pages/MyProfile'))
const ForgotPassword = lazy(() => import('./pages/ForgotPassword'))
const Verify = lazy(() => import('./pages/Verify'))
const UserDashboard = lazy(() => import('./pages/UserDashboard'))
const DoctorDashboard = lazy(() => import('./pages/DoctorDashboard'))
const AdminDashboard = lazy(() => import('./pages/AdminDashboard'))

const LoadingFallback = () => (
  <div className='w-full min-h-[60vh] flex flex-col items-center justify-center gap-4'>
    <div className='w-12 h-12 border-4 border-gray-200 border-t-primary rounded-full animate-spin'></div>
    <p className='text-gray-500 font-medium animate-pulse'>Loading Page...</p>
  </div>
)

const ProtectedRoute = ({ children, allowedRoles }) => {
  const { user, token } = useContext(AppContext)
  if (!token) return <Login />
  
  const userRole = user?.role?.toUpperCase()
  if (allowedRoles && !allowedRoles.map(r => r.toUpperCase()).includes(userRole)) {
    return <div className='py-20 text-center text-red-500'>Access Denied</div>
  }
  return children
}

const App = () => {

  const { user, authLoading } = useContext(AppContext)

  if (authLoading) {
    return (
      <>
        <ToastContainer />
        <Navbar />
        <div className='mx-4 sm:mx-[10%] py-20 text-center text-gray-500'>Loading...</div>
      </>
    )
  }

  return (
    <>
      <ToastContainer />
      <Navbar />

      <div className='mx-4 sm:mx-[5%] md:mx-[8%] lg:mx-[10%]'>
        <Suspense fallback={<LoadingFallback />}>
          <Routes>
            <Route path='/' element={<Home />} />
            <Route path='/doctors' element={<Doctors />} />
            <Route path='/doctors/:speciality' element={<Doctors />} />
            <Route path='/login' element={<Login />} />
            <Route path='/about' element={<About />} />
            <Route path='/contact' element={<Contact />} />
            <Route path='/forgot-password' element={<ForgotPassword />} />
            
            <Route path='/appointment/:docId' element={<ProtectedRoute allowedRoles={['PATIENT', 'USER']}><Appointment /></ProtectedRoute>} />
            <Route path='/my-appointments' element={<ProtectedRoute allowedRoles={['PATIENT', 'USER']}><MyAppointments /></ProtectedRoute>} />
            <Route path='/my-profile' element={<ProtectedRoute><MyProfile /></ProtectedRoute>} />
            <Route path='/verify' element={<Verify />} />

            <Route
              path='/dashboard'
              element={
                user?.role?.toUpperCase() === "DOCTOR"
                  ? <DoctorDashboard />
                  : user?.role?.toUpperCase() === "ADMIN"
                    ? <AdminDashboard />
                    : (user?.role?.toUpperCase() === "PATIENT" || user?.role?.toUpperCase() === "USER")
                      ? <UserDashboard />
                      : <Login />
              }
            />

            <Route path='/admin-dashboard' element={<ProtectedRoute allowedRoles={['ADMIN']}><AdminDashboard /></ProtectedRoute>} />
            <Route path='/doctor-dashboard' element={<ProtectedRoute allowedRoles={['DOCTOR']}><DoctorDashboard /></ProtectedRoute>} />
          </Routes>
        </Suspense>

        <Footer />
      </div>
    </>
  )
}

export default App